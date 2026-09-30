package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "departments", schema = "hr_service")
public class departments extends BaseEntity {


    @Column(name = "dept_code", nullable = false, length = 255)
    String deptCode;

    @Column(name = "parent_code", length = 255)
    String parentCode;

    @Column(name = "name", nullable = false, length = 200)
    String name;

    @Column(name = "code", nullable = false, length = 20)
    String code;

    @Column(name = "head_code", length = 255)
    String headCode;

    @Column(name = "level", nullable = false)
    Short level;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
