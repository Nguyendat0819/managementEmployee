package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/employee")
public interface EmployeeApi {
    @GetMapping("/test")
    ApiResponse<String> test();
}