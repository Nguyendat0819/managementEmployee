package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.controller.api.RoleApi;
import com.example.employee_service.feature.model.request.RoleCreateRequest;
import com.example.employee_service.feature.model.request.RoleSearchRequest;
import com.example.employee_service.feature.model.request.RoleUpdateRequest;
import com.example.employee_service.feature.model.response.PositionResponse;
import com.example.employee_service.feature.model.response.RoleResponse;
import com.example.employee_service.feature.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController implements RoleApi {
    RoleService roleService;
    ApiResponseFactory apiResponseFactory;

    @Override
    public ApiResponse<PageResponse<RoleResponse>> searchRoles(RoleSearchRequest request) {
        return apiResponseFactory.success(roleService.searchRoles(request));
    }

    @Override
    public ApiResponse<RoleResponse> getRole(Long id) {
        return apiResponseFactory.success(roleService.getRole(id));
    }

    @Override
    public ApiResponse<RoleResponse> createRole(RoleCreateRequest request) {
        return apiResponseFactory.success(roleService.createRole(request));
    }

    @Override
    public ApiResponse<RoleResponse> updateRole(Long id, RoleUpdateRequest request) {
        return apiResponseFactory.success(roleService.updateRole(id, request));
    }

    @Override
    public ApiResponse<Void> deleteRole(Long id) {
        roleService.deleteRole(id);
        return apiResponseFactory.success();
    }

    @Override
    public ApiResponse<List<PositionResponse>> getPositions() {
        return apiResponseFactory.success(roleService.getPositions());
    }
}
