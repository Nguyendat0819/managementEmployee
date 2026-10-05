package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
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
public class LeaveTypes extends BaseEntity {


    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "max_days_per_year", nullable = false)
    Integer maxDaysPerYear;


    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
