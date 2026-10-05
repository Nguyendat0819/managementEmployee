package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/role-permission")
public interface RolePermissionApi {
    @Operation(summary = "Lấy permission của nhiều roles cùng lúc (batch)",
            description = "Truyền list roleCode qua query param, trả về Map<roleCode, List<permissionCode>>.")
    @GetMapping(value = "", params = "roleCodes")
    ApiResponse<List<RolePermissionDto>> getPermissionsByRoleCodes(
            @RequestParam("roleCodes") List<String> roleCodes);


    @Operation(summary = "Lấy quyền của người dùng hiện tại",
            description = "Lấy quyền của người dùng hiện tại")
    @GetMapping(value = "/current-user")
    ApiResponse<List<RolePermissionDto>> getPermissionWithCurrentUser();
}
