package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.controller.api.EmployeeApi;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.model.request.EmployeeRequest;
import com.example.employee_service.feature.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class EmployeeController implements EmployeeApi {

    private final ApiResponseFactory apiResponseFactory;
    private final EmployeeService employeeService;

    @Override
    public ApiResponse<List<EmployeeResponse>> getEmployees(String search, String departmentCode, String positionCode, String status, String employmentType) {
        return apiResponseFactory.success(employeeService.getEmployees(search, departmentCode, positionCode, status, employmentType));
    }

    @Override
    public ApiResponse<PageResponse<EmployeeResponse>> searchEmployees(EmployeeSearchRequest request) {
        return apiResponseFactory.success(employeeService.searchEmployees(request));
    }

    @Override
    public ApiResponse<EmployeeResponse> getEmployee(Long id) {
        return apiResponseFactory.success(employeeService.getEmployee(id));
    }

    @Override
    public ApiResponse<EmployeeResponse> createEmployee(EmployeeRequest request) {
        return apiResponseFactory.success(employeeService.createEmployee(request));
    }

    @Override
    public ApiResponse<EmployeeResponse> updateEmployee(Long id, EmployeeRequest request) {
        return apiResponseFactory.success(employeeService.updateEmployee(id, request));
    }

    @Override
    public ApiResponse<Void> deleteEmployee(Long id) {
        employeeService.deleteEmployee(id);
        return apiResponseFactory.success();
    }

    @Override
    public ApiResponse<String> test() {
        return apiResponseFactory.success("Employee controller is up and running");
    }
}
