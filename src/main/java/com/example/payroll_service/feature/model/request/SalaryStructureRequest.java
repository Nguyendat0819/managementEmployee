package com.example.payroll_service.feature.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryStructureRequest(
        @NotBlank @Size(max = 50) String positionCode,
        @NotBlank @Size(max = 150) String positionName,
        @NotBlank @Size(max = 50) String grade,
        @NotNull @DecimalMin("0.00") BigDecimal baseSalary,
        @NotNull @DecimalMin("0.00") BigDecimal allowanceTotal,
        @Size(max = 250) String allowanceSummary,
        @NotNull LocalDate effectiveDate,
        @NotBlank @Pattern(regexp = "Đang áp dụng|Chờ áp dụng|Ngừng áp dụng") String status,
        @NotBlank @Size(max = 200) String formula,
        @Size(max = 500) String note
) {
}
