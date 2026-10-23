package com.example.leave_service.feature.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "leave_balances", schema = "hr_service")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "employee_code", length = 255)
    String employeeCode;

    @Column(name = "requester_user_id")
    Long requesterUserId;

    @Column(name = "type")
    Long type;

    @Column(name = "balance_year", nullable = false)
    Integer balanceYear;

    @Column(name = "total_days", nullable = false)
    BigDecimal totalDays;

    @Column(name = "used_days", nullable = false)
    BigDecimal usedDays;
}
