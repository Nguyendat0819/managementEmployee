package com.example.attendance_service.feature.model.response;

import com.example.attendance_service.feature.entity.Shift;

import java.time.LocalTime;
import java.util.List;

public record ShiftResponse(
        Long id,
        String shiftName,
        LocalTime startTime,
        LocalTime endTime,
        List<String> dayOfWeek,
        boolean active,
        long assignedCount
) {
    public static ShiftResponse from(Shift shift, long assignedCount) {
        return new ShiftResponse(shift.getId(), shift.getShiftName(), shift.getStartTime(),
                shift.getEndTime(), shift.getDayOfWeek(), !Boolean.TRUE.equals(shift.getIsDeleted()), assignedCount);
    }
}
