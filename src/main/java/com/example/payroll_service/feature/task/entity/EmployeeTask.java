package com.example.payroll_service.feature.task.entity;

import com.example.payroll_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "employee_tasks", schema = "hr_service")
public class EmployeeTask extends BaseEntity {
    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "title", nullable = false, length = 200)
    String title;

    @Column(name = "description", length = 2000)
    String description;

    @Column(name = "acceptance_criteria", nullable = false, length = 4000)
    String acceptanceCriteria;

    @Column(name = "period_month", nullable = false)
    LocalDate periodMonth;

    @Column(name = "weight", nullable = false, precision = 7, scale = 2)
    BigDecimal weight;

    @Column(name = "due_date")
    LocalDate dueDate;

    @Column(name = "bonus_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal bonusAmount;

    @Column(name = "penalty_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal penaltyAmount;

    @Column(name = "status", nullable = false, length = 30)
    String status;

    @Column(name = "submitted_at")
    LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;

    @Column(name = "reviewed_by", length = 255)
    String reviewedBy;

    @Column(name = "review_comment", length = 1000)
    String reviewComment;
}
