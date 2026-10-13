package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.PermissionCreateRequest;
import com.example.employee_service.feature.model.request.PermissionSearchRequest;
import com.example.employee_service.feature.model.request.PermissionUpdateRequest;
import com.example.employee_service.feature.model.response.PermissionResponse;

public interface PermissionService {
    PageResponse<PermissionResponse> search(PermissionSearchRequest request);

    PermissionResponse getPermission(Long id);

    PermissionResponse createPermission(PermissionCreateRequest request);

    PermissionResponse updatePermission(Long id, PermissionUpdateRequest request);

    void deletePermission(Long id);
}
