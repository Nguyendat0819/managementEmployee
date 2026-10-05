package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "role_permissions", schema = "hr_service")
public class role_permissions extends BaseEntity {

    @Column(name = "role_code", nullable = false, length = 255)
    String roleCode;

    @Column(name = "permission_code", nullable = false, length = 50)
    String permissionCode;
}