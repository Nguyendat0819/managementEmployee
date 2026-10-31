package com.example.attendance_service.feature.model.response;

import com.example.attendance_service.feature.entity.AttendanceRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record AttendanceResponse(
        Long id,
        String employeeCode,
        Long shiftId,
        String shiftName,
        LocalTime scheduledStartTime,
        LocalTime scheduledEndTime,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        Integer workDurationMin,
        BigDecimal workDurationHours,
        Integer overtimeMin,
        Integer lateMinutes,
        Integer earlyMinutes,
        String status,
        String note,
        LocalDate recordedAt
) {
    public static AttendanceResponse from(AttendanceRecord record) {
        Integer overtimeMin = record.getOvertimeMin();
        if (overtimeMin != null) {
            int workDurationMin = Math.max(record.getWorkDurationMin() == null
                    ? 0 : record.getWorkDurationMin(), 0);
            overtimeMin = record.getShiftId() == null
                    ? 0
                    : Math.min(Math.max(overtimeMin, 0), workDurationMin);
        }
        return new AttendanceResponse(
                record.getId(),
                record.getEmployeeCode(),
                record.getShiftId(),
                record.getShiftName(),
                record.getScheduledStartTime(),
                record.getScheduledEndTime(),
                record.getCheckIn(),
                record.getCheckOut(),
                record.getWorkDurationMin(),
                record.getWorkDurationMin() == null ? null : BigDecimal.valueOf(record.getWorkDurationMin())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP),
                overtimeMin,
                record.getLateMinutes(),
                record.getEarlyMinutes(),
                record.getShiftId() == null ? "NO_SHIFT" : record.getStatus(),
                record.getNote(),
                record.getRecordedAt()
        );
    }
}
