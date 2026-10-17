package com.example.payroll_service.feature.entity;

import com.example.payroll_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "payroll_holidays", schema = "hr_service")
public class PayrollHoliday extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    String name;

    @Column(name = "holiday_type", nullable = false, length = 80)
    String holidayType;

    @Column(name = "holiday_date", nullable = false)
    LocalDate holidayDate;

    @Column(name = "holiday_date_end", nullable = false)
    LocalDate holidayDateEnd;

    @Column(name = "notes", length = 500)
    String notes;
}
