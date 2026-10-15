package com.example.attendance_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ShiftAssignmentRequest(
        @NotBlank @Size(max = 50) String employeeCode,
        @NotNull Long shiftId,
        @NotNull LocalDate effectiveDate,
        LocalDate endDate
) {
}
