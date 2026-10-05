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
@Table(name = "permissions", schema = "hr_service")
public class Permissions extends BaseEntity {

    @Column(name = "permission_code", nullable = false, length = 50)
    String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 50)
    String permissionName;

    @Column(name = "description", length = 255)
    String description;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}