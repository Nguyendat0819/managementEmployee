package com.example.payroll_service.feature.client;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.feature.model.response.EmployeeProfileResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface AdminManagerClient {
    @GetExchange("/employee-service/api/employee/me")
    ApiResponse<EmployeeProfileResponse> getCurrentEmployee();
}
