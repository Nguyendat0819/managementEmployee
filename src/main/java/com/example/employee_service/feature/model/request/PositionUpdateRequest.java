package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionUpdateRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 50)
    private String level;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal baseSalaryMin;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal baseSalaryMax;
}
