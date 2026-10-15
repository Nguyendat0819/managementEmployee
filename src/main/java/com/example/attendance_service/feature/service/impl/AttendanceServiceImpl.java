package com.example.attendance_service.feature.service.impl;

import com.example.attendance_service.common.exception.BusinessException;
import com.example.attendance_service.common.exception.ResourceNotFoundException;
import com.example.attendance_service.common.response.DomainCode;
import com.example.attendance_service.feature.entity.AttendanceRecord;
import com.example.attendance_service.feature.entity.EmployeeShiftAssignment;
import com.example.attendance_service.feature.entity.Shift;
import com.example.attendance_service.feature.model.request.CheckInRequest;
import com.example.attendance_service.feature.model.request.CheckOutRequest;
import com.example.attendance_service.feature.model.response.AttendanceResponse;
import com.example.attendance_service.feature.repository.AttendanceRecordRepository;
import com.example.attendance_service.feature.repository.EmployeeShiftAssignmentRepository;
import com.example.attendance_service.feature.repository.ShiftRepository;
import com.example.attendance_service.feature.service.AttendanceService;
import com.example.attendance_service.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final AttendanceRecordRepository repository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final ShiftRepository shiftRepository;

    @Override
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        LocalDate today = now.toLocalDate();
        if (repository.findByEmployeeCodeAndRecordedAt(request.employeeCode(), today).isPresent()) {
            throw new BusinessException(DomainCode.CONFLICT, "Nhân viên đã có bản ghi chấm công trong ngày hôm nay");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeCode(request.employeeCode());
        record.setCheckIn(now);
        record.setNote(request.note());
        record.setRecordedAt(today);
        applyAssignedShift(record, now, today);
        record.setStatus(record.getShiftId() == null
                ? "NO_SHIFT"
                : record.getLateMinutes() != null && record.getLateMinutes() > 0 ? "LATE" : "PRESENT");
        return AttendanceResponse.from(repository.save(record));
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request) {
        AttendanceRecord record = repository
                .findFirstByEmployeeCodeAndCheckOutIsNullOrderByRecordedAtDesc(request.employeeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi check-in hôm nay"));

        if (record.getCheckOut() != null) {
            throw new BusinessException(DomainCode.CONFLICT, "Nhân viên đã check-out trong ngày hôm nay");
        }

        LocalDateTime checkOut = LocalDateTime.now(BUSINESS_ZONE);
        if (checkOut.isBefore(record.getCheckIn())) {
            throw new BusinessException(DomainCode.BAD_REQUEST, "Thời điểm check-out không hợp lệ");
        }
        int duration = (int) Duration.between(record.getCheckIn(), checkOut).toMinutes();
        record.setCheckOut(checkOut);
        record.setWorkDurationMin(Math.max(duration, 0));
        LocalDateTime scheduledEnd = scheduledEnd(record);
        int overtime = scheduledEnd == null ? 0 : (int) Duration.between(scheduledEnd, checkOut).toMinutes();
        record.setOvertimeMin(Math.max(overtime, 0));
        record.setNote(request.note());
        if (scheduledEnd == null || record.getShiftId() == null) {
            record.setStatus("NO_SHIFT");
        } else if (record.getLateMinutes() != null && record.getLateMinutes() > 0) {
            record.setStatus("LATE");
        } else if (checkOut.isBefore(scheduledEnd)) {
            record.setStatus("INCOMPLETE");
        } else {
            record.setStatus("PRESENT");
        }
        return AttendanceResponse.from(repository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> history(String employeeCode, LocalDate from, LocalDate to) {
        validateRange(from, to);
        return repository.findByEmployeeCodeAndRecordedAtBetweenOrderByRecordedAtDesc(employeeCode, from, to)
                .stream()
                .map(AttendanceResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> search(LocalDate from, LocalDate to, String employeeCode,
                                                   String status, int page, int size) {
        validateRange(from, to);
        if (page < 0 || size < 1 || size > 200) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "page/size");
        }
        return PageResponse.of(repository.search(from, to, employeeCode, status,
                PageRequest.of(page, size, Sort.unsorted())).map(AttendanceResponse::from));
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "from/to");
        }
    }

    private void applyAssignedShift(AttendanceRecord record, LocalDateTime checkIn, LocalDate workDate) {
        List<EmployeeShiftAssignment> assignments = assignmentRepository
                .findActiveAssignments(record.getEmployeeCode(), workDate);
        if (assignments.isEmpty()) {
            return;
        }

        EmployeeShiftAssignment assignment = assignments.get(0);
        Shift shift = shiftRepository.findById(assignment.getShift())
                .filter(value -> !Boolean.TRUE.equals(value.getIsDeleted()))
                .orElse(null);
        if (shift == null || shift.getStartTime() == null || shift.getEndTime() == null) {
            return;
        }
        if (!isScheduledDay(shift, workDate)) {
            return;
        }

        LocalDateTime scheduledStart = workDate.atTime(shift.getStartTime());
        long difference = Duration.between(scheduledStart, checkIn).toMinutes();
        record.setShiftId(shift.getId());
        record.setShiftName(shift.getShiftName());
        record.setScheduledStartTime(shift.getStartTime());
        record.setScheduledEndTime(shift.getEndTime());
        record.setLateMinutes((int) Math.max(difference, 0));
        record.setEarlyMinutes((int) Math.max(-difference, 0));
    }

    private LocalDateTime scheduledEnd(AttendanceRecord record) {
        if (record.getRecordedAt() == null || record.getScheduledEndTime() == null) {
            return null;
        }
        LocalDateTime end = record.getRecordedAt().atTime(record.getScheduledEndTime());
        if (record.getScheduledStartTime() != null
                && !record.getScheduledEndTime().isAfter(record.getScheduledStartTime())) {
            end = end.plusDays(1);
        }
        return end;
    }

    private boolean isScheduledDay(Shift shift, LocalDate workDate) {
        List<String> dayOfWeek = shift.getDayOfWeek();
        if (dayOfWeek == null || dayOfWeek.isEmpty()) {
            return true;
        }
        String dayCode = workDate.getDayOfWeek().getValue() == 7
                ? "CN"
                : "T" + (workDate.getDayOfWeek().getValue() + 1);
        String dayName = workDate.getDayOfWeek().name();
        return dayOfWeek.stream()
                .filter(day -> day != null)
                .map(day -> day.toUpperCase(Locale.ROOT))
                .anyMatch(day -> day.equals(dayCode) || day.equals(dayName));
    }
}
