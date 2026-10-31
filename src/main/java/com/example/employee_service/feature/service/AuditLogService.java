package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.response.AuditLogResponse;
import com.example.employee_service.feature.model.response.AuditLogSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {

    PageResponse<AuditLogResponse> search(
            String search,
            String module,
            String action,
            String result,
            LocalDate from,
            LocalDate to,
            List<String> sorts,
            int page,
            int size);

    AuditLogSummaryResponse summary();

    void record(Long userId, String action, String entityType, Long entityId,
                String requestUri, String ipAddress, String userAgent, String result);
}
