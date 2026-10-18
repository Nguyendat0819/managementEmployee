package com.example.payroll_service.feature.task.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 255) String employeeCode,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 4000) String acceptanceCriteria,
        @NotNull LocalDate periodMonth,
        LocalDate dueDate,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 5, fraction = 2) BigDecimal weight,
        @NotNull @DecimalMin("0.00") BigDecimal bonusAmount,
        @NotNull @DecimalMin("0.00") BigDecimal penaltyAmount
) {}
