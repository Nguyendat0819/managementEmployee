package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "salary_structures", schema = "hr_service")
public class salary_structures extends BaseEntity {

    @Column(name = "position_code", nullable = false, length = 255)
    String positionCode;

    @Column(name = "base_salary", nullable = false)
    BigDecimal baseSalary;

    @Column(name = "allowances", nullable = false, columnDefinition = "jsonb")
    String allowances;

    @Column(name = "formula_config", nullable = false, columnDefinition = "jsonb")
    String formulaConfig;

    @Column(name = "effective_date", nullable = false)
    LocalDate effectiveDate;
}
