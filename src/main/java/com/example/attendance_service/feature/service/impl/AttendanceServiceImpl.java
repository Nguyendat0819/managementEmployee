package com.example.attendance_service.feature.service.impl;

import com.example.attendance_service.common.exception.BusinessException;
import com.example.attendance_service.common.exception.ResourceNotFoundException;
import com.example.attendance_service.common.response.DomainCode;
import com.example.attendance_service.feature.entity.AttendanceRecord;
import com.example.attendance_service.feature.model.request.CheckInRequest;
import com.example.attendance_service.feature.model.request.CheckOutRequest;
import com.example.attendance_service.feature.model.response.AttendanceResponse;
import com.example.attendance_service.feature.repository.AttendanceRecordRepository;
import com.example.attendance_service.feature.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final int STANDARD_WORK_MINUTES = 8 * 60;

    private final AttendanceRecordRepository repository;

    @Override
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        LocalDate today = LocalDate.now();
        if (repository.findByEmployeeCodeAndRecordedAt(request.employeeCode(), today).isPresent()) {
            throw new BusinessException(DomainCode.CONFLICT, "Nhân viên đã có bản ghi chấm công trong ngày hôm nay");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeCode(request.employeeCode());
        record.setCheckIn(LocalDateTime.now());
        record.setStatus("PRESENT");
        record.setNote(request.note());
        record.setRecordedAt(today);
        return AttendanceResponse.from(repository.save(record));
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request) {
        AttendanceRecord record = repository
                .findByEmployeeCodeAndRecordedAt(request.employeeCode(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi check-in hôm nay"));

        if (record.getCheckOut() != null) {
            throw new BusinessException(DomainCode.CONFLICT, "Nhân viên đã check-out trong ngày hôm nay");
        }

        LocalDateTime checkOut = LocalDateTime.now();
        int duration = (int) Duration.between(record.getCheckIn(), checkOut).toMinutes();
        record.setCheckOut(checkOut);
        record.setWorkDurationMin(Math.max(duration, 0));
        record.setOvertimeMin(Math.max(duration - STANDARD_WORK_MINUTES, 0));
        record.setNote(request.note());
        record.setStatus(duration < STANDARD_WORK_MINUTES ? "INCOMPLETE" : "PRESENT");
        return AttendanceResponse.from(repository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> history(String employeeCode, LocalDate from, LocalDate to) {
        return repository.findByEmployeeCodeAndRecordedAtBetweenOrderByRecordedAtDesc(employeeCode, from, to)
                .stream()
                .map(AttendanceResponse::from)
                .toList();
    }
}
