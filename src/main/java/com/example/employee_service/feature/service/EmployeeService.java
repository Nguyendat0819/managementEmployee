package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.model.response.EmployeeCatalogResponse;
import com.example.employee_service.feature.model.response.EmployeeUserOptionResponse;
import com.example.employee_service.feature.model.request.EmployeeRequest;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse getCurrentEmployee(Long userId, String email);
    EmployeeCatalogResponse getCatalogs();
    List<EmployeeUserOptionResponse> getUnlinkedUsers();
    List<EmployeeResponse> getEmployees(String search, String departmentCode, String positionCode, String status, String employmentType);
    PageResponse<EmployeeResponse> searchEmployees(EmployeeSearchRequest request);
    EmployeeResponse getEmployee(Long id);
    EmployeeResponse createEmployee(EmployeeRequest request);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);
}
