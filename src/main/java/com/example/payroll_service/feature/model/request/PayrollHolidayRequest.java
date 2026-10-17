package com.example.payroll_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PayrollHolidayRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 80) String holidayType,
        @NotNull LocalDate holidayDate,
        @NotNull LocalDate holidayDateEnd,
        @Size(max = 500) String notes
) {
}
