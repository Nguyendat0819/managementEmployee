package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.request.DepartmentsRequest;
import com.example.employee_service.feature.model.request.DepartmentsUpdateRequest;
import com.example.employee_service.feature.model.response.DepartmentResponse;
import com.example.employee_service.feature.model.response.EmployeeResponse;

import java.util.List;

public interface OrganizationService {
    DepartmentResponse createDepartment( DepartmentsRequest request);

    DepartmentResponse updateDepartment( String deptCode, DepartmentsUpdateRequest request);

    void deleteDepartment(String deptCode);

    List<DepartmentResponse> getDepartments(
            String search,
            Short level,
            String parentCode,
            Boolean isDeleted
    );

    List<EmployeeResponse> getEmployeesByDepartment(String departmentCode);
}
