package com.example.employee_service.feature.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeUserOptionResponse {
    private Long id;
    private String userName;
}
