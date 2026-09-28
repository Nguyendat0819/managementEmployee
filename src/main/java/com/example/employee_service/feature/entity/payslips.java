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

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "payslips", schema = "Hr_service")
public class payslips extends BaseEntity {

    @Column(name = "run", nullable = false)
    Long run;

    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "base_salary", nullable = false)
    BigDecimal baseSalary;

    @Column(name = "total_allowance", nullable = false)
    BigDecimal totalAllowance;

    @Column(name = "overtime_pay", nullable = false)
    BigDecimal overtimePay;

    @Column(name = "bonus", nullable = false)
    BigDecimal bonus;

    @Column(name = "gross_salary", nullable = false)
    BigDecimal grossSalary;

    @Column(name = "social_insurance", nullable = false)
    BigDecimal socialInsurance;

    @Column(name = "health_insurance", nullable = false)
    BigDecimal healthInsurance;

    @Column(name = "pit_deduction", nullable = false)
    BigDecimal pitDeduction;

    @Column(name = "other_deduction", nullable = false)
    BigDecimal otherDeduction;

    @Column(name = "net_salary", nullable = false)
    BigDecimal netSalary;

    @Column(name = "working_days", nullable = false)
    Short workingDays;

    @Column(name = "absent_days", nullable = false)
    Short absentDays;

    @Column(name = "overtime_hours", nullable = false)
    BigDecimal overtimeHours;

    @Column(name = "snapshot_data", nullable = false, columnDefinition = "jsonb")
    String snapshotData;
}
