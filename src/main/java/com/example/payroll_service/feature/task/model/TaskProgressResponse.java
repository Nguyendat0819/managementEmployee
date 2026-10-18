package com.example.payroll_service.feature.task.model;

import java.time.LocalDateTime;
import java.util.List;

public record TaskProgressResponse(
        Long id,
        String result,
        List<String> evidenceLinks,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String reviewedBy,
        String reviewComment
) {}
