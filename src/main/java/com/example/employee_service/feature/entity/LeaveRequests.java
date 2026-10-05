package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
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
@Table(name = "leave_requests", schema = "hr_service")
public class LeaveRequests extends BaseEntity {

    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "type", nullable = false)
    Long type;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    BigDecimal totalDays;

    @Column(name = "reason", columnDefinition = "text")
    String reason;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "approver_code", length = 255)
    String approverCode;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "reject_reason", columnDefinition = "text")
    String rejectReason;
}
