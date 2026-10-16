package com.example.payroll_service.feature.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayrollRunResponse(
        Long id,
        Short periodYear,
        Integer periodMonth,
        String periodLabel,
        String salaryStructureLabel,
        Integer employeeCount,
        BigDecimal totalGross,
        BigDecimal totalDeduction,
        BigDecimal totalNet,
        String status,
        LocalDateTime createdAt,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime paidAt,
        String note
) {
}
