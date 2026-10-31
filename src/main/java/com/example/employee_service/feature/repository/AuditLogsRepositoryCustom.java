package com.example.employee_service.feature.repository;

import com.example.employee_service.feature.entity.AuditLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogsRepositoryCustom {

    Page<AuditLogs> search(String search, String module, String action, String result,
                           LocalDateTime fromInclusive, LocalDateTime toExclusive, Pageable pageable);
}
