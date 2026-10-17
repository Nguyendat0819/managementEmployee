package com.example.payroll_service.feature.model.response;

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
        LocalDate contractStartDate,
        LocalDate contractEndDate
) {
}
