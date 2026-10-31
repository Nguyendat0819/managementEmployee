package com.example.attendance_service.feature.service.impl;

import com.example.attendance_service.feature.entity.AuditLogs;
import com.example.attendance_service.feature.repository.AuditLogsRepository;
import com.example.attendance_service.feature.service.AuditLogRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogRecorderImpl implements AuditLogRecorder {

    private final AuditLogsRepository auditLogsRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String entityType, Long entityId,
                       String requestUri, String ipAddress, String userAgent, String result) {
        AuditLogs log = new AuditLogs();
        log.setUser(userId);
        log.setAction(limit(action, 50));
        log.setEntityType(limit(entityType, 50));
        log.setEntityId(entityId);
        log.setRequestUri(limit(requestUri, 500));
        log.setIpAddress(limit(ipAddress, 45));
        log.setUserAgent(limit(userAgent, 500));
        log.setResult("FAILURE".equals(result) ? "FAILURE" : "SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        auditLogsRepository.save(log);
    }

    private String limit(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
