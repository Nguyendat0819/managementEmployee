package com.example.payroll_service.feature.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryStructureResponse(
        Long id,
        String positionCode,
        String positionName,
        String grade,
        BigDecimal baseSalary,
        BigDecimal allowanceTotal,
        String allowanceSummary,
        LocalDate effectiveDate,
        String status,
        String formula,
        String note
) {
}
