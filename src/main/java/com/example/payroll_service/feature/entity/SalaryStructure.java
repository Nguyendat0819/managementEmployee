package com.example.payroll_service.feature.entity;

import com.example.payroll_service.common.persistence.dto.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "salary_structures", schema = "hr_service")
public class SalaryStructure extends BaseEntity {

    @Column(name = "position_code", nullable = false, length = 255)
    String positionCode;

    @Column(name = "base_salary", nullable = false)
    BigDecimal baseSalary;

    @Column(name = "allowances", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    JsonNode allowances;

    @Column(name = "formula_config", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    JsonNode formulaConfig;

    @Column(name = "effective_date", nullable = false)
    LocalDate effectiveDate;
}
