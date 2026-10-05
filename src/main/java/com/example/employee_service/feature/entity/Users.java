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
@Table(name = "users", schema = "hr_service")
public class Users extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255)
    String email;

    @Column(name = "user_name", nullable = false, length = 255)
    String userName;

    @Column(name = "password_hash", length = 500)
    String passwordHash;

    @Column(name = "role_code", length = 255)
    String roleCode;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "last_login_at")
    LocalDateTime lastLoginAt;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted = false;
}
