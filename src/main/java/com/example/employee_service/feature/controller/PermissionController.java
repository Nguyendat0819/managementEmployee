package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.controller.api.PermissionApi;
import com.example.employee_service.feature.model.request.PermissionCreateRequest;
import com.example.employee_service.feature.model.request.PermissionSearchRequest;
import com.example.employee_service.feature.model.request.PermissionUpdateRequest;
import com.example.employee_service.feature.model.response.PermissionResponse;
import com.example.employee_service.feature.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController implements PermissionApi {
    PermissionService permissionService;
    ApiResponseFactory apiResponseFactory;

    @Override
    public ApiResponse<PageResponse<PermissionResponse>> search(PermissionSearchRequest request) {
        return apiResponseFactory.success(permissionService.search(request));
    }

    @Override
    public ApiResponse<PermissionResponse> getPermission(Long id) {
        return apiResponseFactory.success(permissionService.getPermission(id));
    }

    @Override
    public ApiResponse<PermissionResponse> createPermission(PermissionCreateRequest request) {
        return apiResponseFactory.success(permissionService.createPermission(request));
    }

    @Override
    public ApiResponse<PermissionResponse> updatePermission(Long id, PermissionUpdateRequest request) {
        return apiResponseFactory.success(permissionService.updatePermission(id, request));
    }

    @Override
    public ApiResponse<Void> deletePermission(Long id) {
        permissionService.deletePermission(id);
        return apiResponseFactory.success();
    }
}
