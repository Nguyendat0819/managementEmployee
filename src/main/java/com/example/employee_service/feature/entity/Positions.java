package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "positions", schema = "hr_service")
public class Positions extends BaseEntity {

    @Column(name = "position_code", nullable = false, length = 255)
    String positionCode;

    @Column(name = "title", nullable = false, length = 200)
    String title;

    @Column(name = "level", nullable = false, length = 50)
    String level;

    @Column(name = "base_salary_min", nullable = false)
    BigDecimal baseSalaryMin;

    @Column(name = "base_salary_max", nullable = false)
    BigDecimal baseSalaryMax;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
