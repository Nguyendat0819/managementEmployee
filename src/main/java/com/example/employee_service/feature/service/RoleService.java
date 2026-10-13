package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.RoleCreateRequest;
import com.example.employee_service.feature.model.request.RoleSearchRequest;
import com.example.employee_service.feature.model.request.RoleUpdateRequest;
import com.example.employee_service.feature.model.response.PositionResponse;
import com.example.employee_service.feature.model.response.RoleResponse;

import java.util.List;

public interface RoleService {
    PageResponse<RoleResponse> searchRoles(RoleSearchRequest request);

    RoleResponse getRole(Long id);

    RoleResponse createRole(RoleCreateRequest request);

    RoleResponse updateRole(Long id, RoleUpdateRequest request);

    void deleteRole(Long id);

    List<PositionResponse> getPositions();
}
