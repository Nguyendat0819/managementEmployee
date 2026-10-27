package com.example.attendance_service.feature.service.impl;

import com.example.attendance_service.common.exception.BusinessException;
import com.example.attendance_service.common.exception.ResourceNotFoundException;
import com.example.attendance_service.common.response.DomainCode;
import com.example.attendance_service.feature.entity.AttendanceRecord;
import com.example.attendance_service.feature.entity.EmployeeShiftAssignment;
import com.example.attendance_service.feature.entity.Shift;
import com.example.attendance_service.feature.model.request.CheckInRequest;
import com.example.attendance_service.feature.model.request.CheckOutRequest;
import com.example.attendance_service.feature.model.request.PayrollAttendanceRequest;
import com.example.attendance_service.feature.model.request.PayrollEmployeePeriod;
import com.example.attendance_service.feature.model.response.AttendanceResponse;
import com.example.attendance_service.feature.model.response.PayrollAttendanceSummaryResponse;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        String normalizedEmployeeCode = employeeCode == null
                ? ""
                : employeeCode.trim().toLowerCase(Locale.ROOT);
        String normalizedStatus = status == null ? "" : status.trim();
        return PageResponse.of(repository.search(from, to, normalizedEmployeeCode, normalizedStatus,
                PageRequest.of(page, size, Sort.unsorted())).map(AttendanceResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollAttendanceSummaryResponse> payrollSummary(PayrollAttendanceRequest request) {
        LocalDate from = request.from();
        LocalDate to = request.to();
        validateRange(from, to);
        Map<String, List<PayrollEmployeePeriod>> periodsByCode = request.employeePeriods().stream()
                .filter(period -> period.employeeCode() != null && !period.employeeCode().isBlank()
                        && period.from() != null && period.to() != null && !period.from().isAfter(period.to()))
                .collect(Collectors.groupingBy(period -> normalizeCode(period.employeeCode())));
        Set<String> codes = periodsByCode.keySet().stream()
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (codes.isEmpty() || codes.size() > 1000) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "employeePeriods");
        }

        Set<LocalDate> holidays = new HashSet<>(request.holidays());
        Map<Long, Shift> shifts = shiftRepository.findAllByOrderByIdAsc().stream()
                .filter(shift -> !Boolean.TRUE.equals(shift.getIsDeleted()))
                .collect(Collectors.toMap(Shift::getId, shift -> shift));
        Map<String, List<EmployeeShiftAssignment>> assignments = assignmentRepository
                .findPayrollAssignments(codes, from, to).stream()
                .collect(Collectors.groupingBy(assignment -> normalizeCode(assignment.getEmployeeCode())));
        Map<String, Map<LocalDate, AttendanceRecord>> records = repository.findPayrollRecords(codes, from, to).stream()
                .collect(Collectors.groupingBy(record -> normalizeCode(record.getEmployeeCode()),
                        Collectors.toMap(AttendanceRecord::getRecordedAt, record -> record, (first, ignored) -> first)));

        return codes.stream().sorted().map(code -> summarizeEmployee(code, from, to, holidays,
                periodsByCode.getOrDefault(code, List.of()), assignments.getOrDefault(code, List.of()),
                records.getOrDefault(code, Map.of()), shifts)).toList();
    }

    private PayrollAttendanceSummaryResponse summarizeEmployee(String employeeCode, LocalDate from, LocalDate to,
                                                                 Set<LocalDate> holidays,
                                                                 List<PayrollEmployeePeriod> activePeriods,
                                                                 List<EmployeeShiftAssignment> employeeAssignments,
                                                                 Map<LocalDate, AttendanceRecord> employeeRecords,
                                                                 Map<Long, Shift> shifts) {
        List<EmployeeShiftAssignment> sortedAssignments = employeeAssignments.stream()
                .sorted(Comparator.comparing(EmployeeShiftAssignment::getEffectiveDate).reversed())
                .toList();
        List<String> warnings = new ArrayList<>();
        if (sortedAssignments.isEmpty()) {
            warnings.add("Không có lịch phân ca trong kỳ; hệ thống không suy ra ngày vắng.");
        }

        int scheduledDays = 0;
        int workedDays = 0;
        int paidHolidayDays = 0;
        int standardMinutes = 0;
        int regularOvertime = 0;
        int restOvertime = 0;
        int holidayOvertime = 0;
        Set<LocalDate> scheduledDates = new HashSet<>();
        Set<LocalDate> processedDates = new HashSet<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate workDate = date;
            if (activePeriods.stream().noneMatch(period -> !workDate.isBefore(period.from())
                    && !workDate.isAfter(period.to()))) {
                continue;
            }
            List<EmployeeShiftAssignment> activeAssignments = sortedAssignments.stream()
                    .filter(assignment -> !assignment.getEffectiveDate().isAfter(workDate)
                            && (assignment.getEndDate() == null || !assignment.getEndDate().isBefore(workDate)))
                    .toList();
            if (activeAssignments.isEmpty()) {
                continue;
            }
            if (activeAssignments.size() > 1) {
                warnings.add("Có nhiều ca cùng hiệu lực ngày " + date + "; hệ thống dùng ca có ngày bắt đầu mới nhất.");
            }
            Shift shift = shifts.get(activeAssignments.get(0).getShift());
            if (shift == null || !isScheduledDay(shift, date)) {
                continue;
            }
            scheduledDates.add(date);
            int shiftMinutes = shiftDurationMinutes(shift);
            if (!holidays.contains(date)) {
                scheduledDays++;
                standardMinutes += shiftMinutes;
            }

            AttendanceRecord record = employeeRecords.get(date);
            if (holidays.contains(date)) {
                paidHolidayDays++;
                if (record != null) {
                    processedDates.add(date);
                    if (hasCompletedAttendance(record)) {
                        holidayOvertime += positive(record.getWorkDurationMin());
                    } else if (record.getCheckIn() != null) {
                        warnings.add("Thiếu check-out ngày " + date + ".");
                    }
                }
            } else if (hasCompletedAttendance(record)) {
                workedDays++;
                processedDates.add(date);
                regularOvertime += positive(record.getOvertimeMin());
            } else if (record != null && record.getCheckIn() != null) {
                warnings.add("Thiếu check-out ngày " + date + ".");
            }
        }

        for (Map.Entry<LocalDate, AttendanceRecord> entry : employeeRecords.entrySet()) {
            LocalDate date = entry.getKey();
            AttendanceRecord record = entry.getValue();
            if (processedDates.contains(date) || !hasCompletedAttendance(record)
                    || activePeriods.stream().noneMatch(period -> !date.isBefore(period.from())
                    && !date.isAfter(period.to()))) {
                continue;
            }
            if (holidays.contains(date)) {
                holidayOvertime += positive(record.getWorkDurationMin());
            } else if (scheduledDates.contains(date)) {
                regularOvertime += positive(record.getOvertimeMin());
            } else {
                restOvertime += positive(record.getWorkDurationMin());
            }
        }

        int absentDays = Math.max(scheduledDays - workedDays, 0);
        if (absentDays > 0) {
            warnings.add("Ngày không có chấm công trên ca được tính là vắng; đơn nghỉ/phép hưởng lương chưa được đối chiếu.");
        }
        return new PayrollAttendanceSummaryResponse(employeeCode, scheduledDays, workedDays, absentDays,
                paidHolidayDays, standardMinutes, regularOvertime, restOvertime, holidayOvertime,
                warnings.stream().distinct().toList());
    }

    private boolean hasCompletedAttendance(AttendanceRecord record) {
        return record != null && record.getCheckIn() != null && record.getCheckOut() != null;
    }

    private int positive(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private int shiftDurationMinutes(Shift shift) {
        int minutes = (int) Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        return minutes > 0 ? minutes : minutes + 24 * 60;
    }

    private String normalizeCode(String employeeCode) {
        return employeeCode == null ? "" : employeeCode.trim().toLowerCase(Locale.ROOT);
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
