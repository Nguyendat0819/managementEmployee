package com.example.payroll_service.feature.model.request;

import java.time.LocalDate;
import java.util.List;

public record PayrollAttendanceSummaryRequest(
        LocalDate from,
        LocalDate to,
        List<PayrollEmployeePeriod> employeePeriods,
        List<LocalDate> holidays
) {
}
