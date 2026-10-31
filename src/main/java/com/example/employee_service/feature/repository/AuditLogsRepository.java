package com.example.employee_service.feature.repository;

import com.example.employee_service.feature.entity.AuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogsRepository extends JpaRepository<AuditLogs, Long>, AuditLogsRepositoryCustom {

    long countByResult(String result);

    long countByEntityType(String entityType);
}
