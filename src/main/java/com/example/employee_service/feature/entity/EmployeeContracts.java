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

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "employee_contracts", schema = "hr_service")
public class EmployeeContracts extends BaseEntity {

    @Column(name = "contract_no", nullable = false, length = 50)
    String contractNo;

    @Column(name = "contract_type", nullable = false, length = 50)
    String contractType;

    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "full_name", nullable = false, length = 200)
    String fullName;

    @Column(name = "birth_date")
    LocalDate birthDate;

    @Column(name = "id_card", length = 20)
    String idCard;

    @Column(name = "department_code", length = 255)
    String departmentCode;

    @Column(name = "department_name", length = 200)
    String departmentName;

    @Column(name = "position_code", length = 255)
    String positionCode;

    @Column(name = "position_name", length = 200)
    String positionName;

    @Column(name = "actual_salary", nullable = false)
    BigDecimal actualSalary;

    @Column(name = "employment_type", nullable = false, length = 50)
    String employmentType;

    @Column(name = "hire_date")
    LocalDate hireDate;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "signed_date")
    LocalDate signedDate;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
