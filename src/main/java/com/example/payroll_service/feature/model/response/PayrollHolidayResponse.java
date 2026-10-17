package com.example.payroll_service.feature.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record PayrollHolidayResponse(
        Long id,
        String name,
        String holidayType,
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate holidayDate,
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate holidayDateEnd,
        int dayCount,
        String notes,
        String status
) {
}
