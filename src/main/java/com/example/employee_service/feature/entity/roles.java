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
@Table(name = "roles", schema = "Hr_service")
public class roles extends BaseEntity {

    @Column(name = "role_code", nullable = false, length = 255)
    String rolleCode;

    @Column(name = "role_name", nullable = false, length = 255)
    String roleName;

    @Column(name = "description", length = 255)
    String description;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
