package com.example.leave_service.feature.entity;

import com.example.leave_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "leave_types", schema = "hr_service")
public class LeaveType extends BaseEntity {

    @Column(name = "code", nullable = false, length = 40)
    String code;

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "max_days_per_year", nullable = false)
    Integer maxDaysPerYear;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;

    @Column(name = "carryover_days", nullable = false)
    Integer carryoverDays = 0;

    @Column(name = "paid_leave", nullable = false)
    Boolean paidLeave = true;

    @Column(name = "require_document", nullable = false)
    Boolean requireDocument = false;

    @Column(name = "description", length = 1000)
    String description;
}
