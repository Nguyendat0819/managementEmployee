package com.example.attendance_service.feature.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PayrollAttendanceRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to,
        @NotEmpty @Size(max = 1000) List<@NotNull PayrollEmployeePeriod> employeePeriods,
        @NotNull List<@NotNull LocalDate> holidays
) {
}
