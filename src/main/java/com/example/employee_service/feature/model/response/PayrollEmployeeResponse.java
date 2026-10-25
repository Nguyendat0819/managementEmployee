package com.example.employee_service.feature.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollEmployeeResponse(
        String employeeCode,
        String fullName,
        String departmentCode,
        String departmentName,
        String positionCode,
        String positionName,
        BigDecimal actualSalary,
        String employmentType,
        String contractType,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate contractStartDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate contractEndDate
) {
}
