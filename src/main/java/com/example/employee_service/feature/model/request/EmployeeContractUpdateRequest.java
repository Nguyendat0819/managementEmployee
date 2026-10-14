package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeContractUpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String contractNo;

    @NotBlank
    @Size(max = 50)
    private String contractType;

    @NotBlank
    @Size(max = 255)
    private String employeeCode;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal actualSalary;

    @NotBlank
    @Size(max = 50)
    private String employmentType;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;
    private LocalDate signedDate;

    @NotBlank
    @Size(max = 50)
    private String status;
}
