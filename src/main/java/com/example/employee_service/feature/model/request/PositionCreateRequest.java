package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionCreateRequest {
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới")
    private String positionCode;

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
