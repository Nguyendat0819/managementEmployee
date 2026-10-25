package com.example.employee_service.feature.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    Long id;
    String employeeCode;
    Long user;
    String fullName;
    LocalDate birthDate;
    String gender;
    String idCard;
    String phone;
    String email;
    String departmentCode;
    String departmentName;
    String positionCode;
    String positionName;
    BigDecimal positionSalaryMin;
    BigDecimal positionSalaryMax;
    String employmentType;
    LocalDate hireDate;
    LocalDate contractEnd;
    String status;
    Boolean isDeleted;
}
