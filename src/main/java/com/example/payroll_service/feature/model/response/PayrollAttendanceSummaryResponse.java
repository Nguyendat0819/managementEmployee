package com.example.payroll_service.feature.model.response;

import java.util.List;

public record PayrollAttendanceSummaryResponse(
        String employeeCode,
        int scheduledDays,
        int workedDays,
        int absentDays,
        int paidHolidayDays,
        int standardWorkingMinutes,
        int regularOvertimeMinutes,
        int restDayOvertimeMinutes,
        int holidayOvertimeMinutes,
        List<String> warnings
) {
}
