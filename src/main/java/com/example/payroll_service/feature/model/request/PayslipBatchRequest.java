package com.example.payroll_service.feature.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PayslipBatchRequest(
        @NotEmpty @Size(max = 1000) List<@Valid PayslipLineRequest> entries
) {
}
