package com.example.payroll_service.feature.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PayrollRunRequest(
        @NotNull @Min(2020) @Max(2100) Integer periodYear,
        @NotNull @Min(1) @Max(12) Integer periodMonth,
        @Size(max = 500) String note
) {
}
