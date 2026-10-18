package com.example.notification_service.feature.service;

import com.example.notification_service.common.persistence.dto.RolePermissionDto;

import java.util.List;

public interface RolePermissionService {
    List<RolePermissionDto> getPermissionsByRoleCodes(List<String> roleCodes);
    List<RolePermissionDto> getAllPermissions();
    List<RolePermissionDto> getPermissionWithCurrentUser();
}
