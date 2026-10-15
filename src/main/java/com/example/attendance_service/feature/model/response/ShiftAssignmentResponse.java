package com.example.attendance_service.feature.model.response;

import com.example.attendance_service.feature.entity.EmployeeShiftAssignment;
import com.example.attendance_service.feature.entity.Shift;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ShiftAssignmentResponse(
        Long id,
        String employeeCode,
        Long shiftId,
        String shiftName,
        LocalTime startTime,
        LocalTime endTime,
        List<String> dayOfWeek,
        LocalDate effectiveDate,
        LocalDate endDate
) {
    public static ShiftAssignmentResponse from(EmployeeShiftAssignment assignment, Shift shift) {
        return new ShiftAssignmentResponse(assignment.getId(), assignment.getEmployeeCode(), assignment.getShift(),
                shift == null ? null : shift.getShiftName(), shift == null ? null : shift.getStartTime(),
                shift == null ? null : shift.getEndTime(), shift == null ? null : shift.getDayOfWeek(),
                assignment.getEffectiveDate(), assignment.getEndDate());
    }
}
