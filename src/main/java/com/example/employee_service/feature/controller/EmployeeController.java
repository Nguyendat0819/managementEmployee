package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.feature.controller.api.EmployeeApi;
import com.example.employee_service.feature.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmployeeController implements EmployeeApi {

    private final ApiResponseFactory apiResponseFactory;
    @SuppressWarnings("unused") // injected để Spring scan dependency EmployeeService bean
    private final EmployeeService employeeService;

    @Override
    public ApiResponse<String> test() {
        return apiResponseFactory.success("Employee controller is up and running");
    }
}