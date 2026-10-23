package com.example.leave_service.feature.entity;

import com.example.leave_service.common.persistence.dto.BaseEntity;
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
public class LeaveRequest extends BaseEntity {

    @Column(name = "request_code", nullable = false, unique = true, length = 40)
    String requestCode;

    @Column(name = "requester_user_id")
    Long requesterUserId;

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

    @Column(name = "evidence_url", length = 2000)
    String evidenceUrl;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "approver_code", length = 255)
    String approverCode;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "reject_reason", columnDefinition = "text")
    String rejectReason;
}
