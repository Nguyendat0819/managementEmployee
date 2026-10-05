package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
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
@Table(name = "employees", schema = "hr_service")
public class Employees extends BaseEntity {

    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "user")
    Long user;

    @Column(name = "full_name", nullable = false, length = 200)
    String fullName;

    @Column(name = "birth_date")
    LocalDate birthDate;

    @Column(name = "gender", length = 10)
    String gender;

    @Column(name = "id_card", length = 20)
    String idCard;

    @Column(name = "phone", length = 20)
    String phone;

    @Column(name = "email", length = 255)
    String email;

    @Column(name = "department_code", length = 255)
    String departmentCode;

    @Column(name = "position_code", length = 255)
    String positionCode;

    @Column(name = "employment_type", nullable = false, length = 50)
    String employmentType;

    @Column(name = "hire_date", nullable = false)
    LocalDate hireDate;

    @Column(name = "contract_end")
    LocalDate contractEnd;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
