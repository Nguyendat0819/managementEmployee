package com.example.payroll_service.feature.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PayslipLineRequest(
        @NotBlank @Size(max = 255) String employeeCode,
        @NotNull @DecimalMin("0.00") BigDecimal baseSalary,
        @NotNull @DecimalMin("0.00") BigDecimal totalAllowance,
        @NotNull @DecimalMin("0.00") BigDecimal overtimePay,
        @NotNull @DecimalMin("0.00") BigDecimal bonus,
        @NotNull @DecimalMin("0.00") BigDecimal socialInsurance,
        @NotNull @DecimalMin("0.00") BigDecimal healthInsurance,
        @NotNull @DecimalMin("0.00") BigDecimal unemploymentInsurance,
        @NotNull @DecimalMin("0.00") BigDecimal pitDeduction,
        @NotNull @DecimalMin("0.00") BigDecimal otherDeduction,
        @NotNull @Min(0) @Max(31) Short workingDays,
        @NotNull @Min(0) @Max(31) Short absentDays,
        @NotNull @DecimalMin("0.00") BigDecimal overtimeHours,
        JsonNode snapshotData
) {
}
