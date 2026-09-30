package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "employee_shift_assignments", schema = "hr_service")
public class employee_shift_assignments{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "shift", nullable = false)
    Long shift;

    @Column(name = "effective_date", nullable = false)
    LocalDate effectiveDate;

    @Column(name = "end_date")
    LocalDate endDate;
}
