package com.example.attendance_service.feature.service;

public interface AuditLogRecorder {

    void record(Long userId, String action, String entityType, Long entityId,
                String requestUri, String ipAddress, String userAgent, String result);
}
