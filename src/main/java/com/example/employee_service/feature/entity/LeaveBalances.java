package com.example.employee_service.feature.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;


@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "leave_balances", schema = "hr_service")
public class LeaveBalances {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "employee_code", length = 255)
    String employeeCode;

    @Column(name = "type")
    Long type;


}
