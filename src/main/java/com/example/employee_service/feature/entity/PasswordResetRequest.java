package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "password_reset_requests", schema = "hr_service")
public class PasswordResetRequest extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "requested_at", nullable = false)
    LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;

    @Column(name = "status", nullable = false, length = 20)
    String status;
}
