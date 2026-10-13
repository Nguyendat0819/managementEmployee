package com.example.employee_service.feature.service;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.feature.model.request.UpdateRolePermissionsRequest;

import java.util.List;

public interface RolePermissionService {
    List<RolePermissionDto> getPermissionsByRoleCodes(List<String> roleCodes);
    List<RolePermissionDto> getAllPermissions();
    List<RolePermissionDto> getPermissionWithCurrentUser();

    void assignPermission(String roleCode, String permissionCode);

    void updatePermissions(String roleCode, UpdateRolePermissionsRequest request);

    void deletePermission(String roleCode, String permissionCode);
}
