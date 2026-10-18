package com.example.payroll_service.feature.task.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String employeeCode,
        String title,
        String description,
        String acceptanceCriteria,
        LocalDate periodMonth,
        LocalDate dueDate,
        BigDecimal weight,
        BigDecimal bonusAmount,
        BigDecimal penaltyAmount,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String reviewedBy,
        String reviewComment,
        TaskProgressResponse latestProgress
) {}
