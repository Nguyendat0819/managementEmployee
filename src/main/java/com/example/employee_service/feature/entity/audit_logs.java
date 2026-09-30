package com.example.employee_service.feature.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "audit_logs", schema = "hr_service")
public class audit_logs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "user")
    Long user;

    @Column(name = "action", nullable = false, length = 50)
    String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    String entityType;

    @Column(name = "entity_id", nullable = false)
    Long entityId;

    @Column(name = "old_value", columnDefinition = "jsonb")
    String oldValue;

    @Column(name = "new_value", columnDefinition = "jsonb")
    String newValue;

    @Column(name = "ip_address", length = 45)
    String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
