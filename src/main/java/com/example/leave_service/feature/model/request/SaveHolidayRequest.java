package com.example.leave_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SaveHolidayRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 50) String type,
        @Size(max = 500) String notes
) {
}
