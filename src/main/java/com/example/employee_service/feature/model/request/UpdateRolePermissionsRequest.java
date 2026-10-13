package com.example.employee_service.feature.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionsRequest {
    private List<String> permissionCodes;
    private List<String> permissionCodesToAdd;
    private List<String> permissionCodesToRemove;
}
