package com.example.leave_service.feature.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveLeaveTypeRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(0) Integer annualQuota,
        @NotNull @Min(0) Integer carryoverDays,
        @NotNull Boolean paidLeave,
        @NotNull Boolean requireDocument,
        @NotNull Boolean active,
        @Size(max = 1000) String description
) {
}
