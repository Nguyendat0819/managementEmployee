package com.example.employee_service.feature.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeContractResponse {
    Long id;
    String contractNo;
    String contractType;
    String employeeCode;
    String fullName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate birthDate;
    String idCard;
    String departmentCode;
    String departmentName;
    String positionCode;
    String positionName;
    BigDecimal actualSalary;
    String employmentType;
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate hireDate;
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate startDate;
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate endDate;
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate signedDate;
    String status;
    Boolean isDeleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
