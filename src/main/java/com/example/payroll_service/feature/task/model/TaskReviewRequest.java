package com.example.payroll_service.feature.task.model;

import jakarta.validation.constraints.Size;

public record TaskReviewRequest(@Size(max = 1000) String comment) {
    public boolean hasReason() {
        return comment != null && !comment.isBlank();
    }
}
