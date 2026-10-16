package com.example.payroll_service.feature.model.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PayslipResponse(
        Long id,
        Long payrollRunId,
        String employeeCode,
        Short periodYear,
        Integer periodMonth,
        LocalDate periodStart,
        LocalDate periodEnd,
        String status,
        LocalDateTime paymentDate,
        BigDecimal baseSalary,
        BigDecimal totalAllowance,
        BigDecimal overtimePay,
        BigDecimal bonus,
        BigDecimal grossSalary,
        BigDecimal socialInsurance,
        BigDecimal healthInsurance,
        BigDecimal pitDeduction,
        BigDecimal otherDeduction,
        BigDecimal totalDeduction,
        BigDecimal netSalary,
        Short workingDays,
        Short absentDays,
        BigDecimal overtimeHours,
        JsonNode snapshotData
) {
}
