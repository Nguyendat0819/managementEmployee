package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "payroll_runs", schema = "hr_service")
public class PayrollRuns extends BaseEntity {

    @Column(name = "period_year", nullable = false)
    Short periodYear;

    @Column(name = "period_month", nullable = false)
    Integer periodMonth;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "total_gross")
    BigDecimal totalGross;

    @Column(name = "total_net")
    BigDecimal totalNet;

    @Column(name = "created_by_employee_code", length = 255)
    String createdByEmployeeCode;

    @Column(name = "approved_by", length = 255)
    String approvedBy;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "paid_at")
    LocalDateTime paidAt;
}
