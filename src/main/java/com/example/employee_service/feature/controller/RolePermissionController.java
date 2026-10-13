package com.example.employee_service.feature.controller;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.feature.controller.api.RolePermissionApi;
import com.example.employee_service.feature.model.request.UpdateRolePermissionsRequest;
import com.example.employee_service.feature.model.request.AssignPermissionRequest;
import com.example.employee_service.feature.service.RolePermissionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionController implements RolePermissionApi {
    ApiResponseFactory apiResponseFactory;
    RolePermissionService rolePermissionService;

    @Override
    public ApiResponse<List<RolePermissionDto>> getPermissionsByRoleCodes(List<String> roleCodes) {
        return apiResponseFactory.success(rolePermissionService.getPermissionsByRoleCodes(roleCodes));
    }
    @Override
    public ApiResponse<List<RolePermissionDto>> getPermissionWithCurrentUser(){
        return apiResponseFactory.success(rolePermissionService.getPermissionWithCurrentUser());
    }

    @Override
    public ApiResponse<List<RolePermissionDto>> getAllRolePermission() {
        return apiResponseFactory.success(rolePermissionService.getAllPermissions());
    }

    @Override
    public ApiResponse<Void> assignPermission(String roleCode, AssignPermissionRequest request) {
        rolePermissionService.assignPermission(roleCode, request.getPermissionCode());
        return apiResponseFactory.success();
    }

    @Override
    public ApiResponse<Void> updatePermissions(String roleCode, UpdateRolePermissionsRequest request) {
        rolePermissionService.updatePermissions(roleCode, request);
        return apiResponseFactory.success();
    }

    @Override
    public ApiResponse<Void> deletePermission(String roleCode, String permissionCode) {
        rolePermissionService.deletePermission(roleCode, permissionCode);
        return apiResponseFactory.success();
    }
}
