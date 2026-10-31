package com.example.attendance_service.feature.repository;

import com.example.attendance_service.feature.entity.AuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogsRepository extends JpaRepository<AuditLogs, Long> {
}
