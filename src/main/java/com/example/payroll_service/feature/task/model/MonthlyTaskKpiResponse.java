package com.example.payroll_service.feature.task.model;

import java.time.LocalDate;
import java.math.BigDecimal;

public record MonthlyTaskKpiResponse(
        String employeeCode,
        LocalDate periodMonth,
        long taskCount,
        long completedTaskCount,
        long incompleteTaskCount,
        long pendingReviewCount,
        BigDecimal completedWeight,
        BigDecimal totalWeight
) {}
