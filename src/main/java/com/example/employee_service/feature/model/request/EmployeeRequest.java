package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {
    private Long userId;
    @NotBlank @Size(max = 255)
    private String employeeCode;
    @NotBlank @Size(max = 200)
    private String fullName;
    private LocalDate birthDate;
    @Size(max = 10)
    private String gender;
    @Size(max = 20)
    private String idCard;
    @Size(max = 20)
    private String phone;
    @Email @Size(max = 255)
    private String email;
    @NotBlank @Size(max = 255)
    private String departmentCode;
    @NotBlank @Size(max = 255)
    private String positionCode;
    @NotBlank @Size(max = 50)
    private String employmentType;
    @NotNull
    private LocalDate hireDate;
    private LocalDate contractEnd;
    @NotBlank @Size(max = 50)
    private String status;
}
