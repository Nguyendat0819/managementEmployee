package com.example.employee_service.feature.model.response;

public record AuditLogSummaryResponse(
        long total,
        long successful,
        long failed,
        long securityEvents) {
}
