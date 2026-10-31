package com.example.employee_service.feature.model.response;

public record AuditLogResponse(
        Long id,
        String occurredAt,
        String actor,
        String actorName,
        String action,
        String module,
        String target,
        String result,
        String ipAddress,
        String device,
        String details) {
}
