package com.example.attendance_service.feature.model.response;

import com.example.attendance_service.feature.entity.AttendanceRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        String employeeCode,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        Integer workDurationMin,
        Integer overtimeMin,
        String status,
        String note,
        LocalDate recordedAt
) {
    public static AttendanceResponse from(AttendanceRecord record) {
        return new AttendanceResponse(
                record.getId(),
                record.getEmployeeCode(),
                record.getCheckIn(),
                record.getCheckOut(),
                record.getWorkDurationMin(),
                record.getOvertimeMin(),
                record.getStatus(),
                record.getNote(),
                record.getRecordedAt()
        );
    }
}
