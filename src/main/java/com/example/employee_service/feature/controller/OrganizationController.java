package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.feature.controller.api.OrganizationApi;
import com.example.employee_service.feature.model.request.DepartmentsRequest;
import com.example.employee_service.feature.model.request.DepartmentsUpdateRequest;
import com.example.employee_service.feature.model.response.DepartmentResponse;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.service.OrganizationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationController implements OrganizationApi {

    ApiResponseFactory apiResponseFactory;
    OrganizationService organizationService;

    @Override
    public ApiResponse<DepartmentResponse> createDepartment(DepartmentsRequest request) {
        return apiResponseFactory.success(organizationService.createDepartment(request));
    }

    @Override
    public ApiResponse<DepartmentResponse> updateDepartment(String deptCode, DepartmentsUpdateRequest request){
        return apiResponseFactory.success(organizationService.updateDepartment(deptCode, request));
    }

    @Override
    public ApiResponse<Void> deleteDepartment(String deptCode) {
        organizationService.deleteDepartment(deptCode);
        return apiResponseFactory.success();
    }

    @Override
    public ApiResponse<List<DepartmentResponse>> getDepartments(
            String search,
            Short level,
            String parentCode,
            Boolean isDeleted
    ) {
        return apiResponseFactory.success(
                organizationService.getDepartments(search, level, parentCode, isDeleted)
        );
    }

    @Override
    public ApiResponse<List<EmployeeResponse>> getEmployeesByDepartment(String departmentCode) {
        return apiResponseFactory.success(organizationService.getEmployeesByDepartment(departmentCode));
    }
}
