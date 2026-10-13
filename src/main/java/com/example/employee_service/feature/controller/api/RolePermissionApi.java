package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.request.AssignPermissionRequest;
import com.example.employee_service.feature.model.request.UpdateRolePermissionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/role-permission")
public interface RolePermissionApi {
    @Operation(summary = "Lấy permission của nhiều roles cùng lúc (batch)",
            description = "Truyền list roleCode qua query param, trả về Map<roleCode, List<permissionCode>>.")
    @GetMapping(value = "", params = "roleCodes")
    @RequiresPermission(PermissionDefine.ROLE_PERMISSION_READ)
    ApiResponse<List<RolePermissionDto>> getPermissionsByRoleCodes(
            @RequestParam("roleCodes") List<String> roleCodes);


    @Operation(summary = "Lấy quyền của người dùng hiện tại",
            description = "Lấy quyền của người dùng hiện tại")
    @GetMapping(value = "/current-user")
    ApiResponse<List<RolePermissionDto>> getPermissionWithCurrentUser();

    @Operation(summary = "Lấy toàn bộ mapping role-permission đang hoạt động")
    @GetMapping("/all")
    @RequiresPermission(PermissionDefine.ROLE_PERMISSION_READ)
    ApiResponse<List<RolePermissionDto>> getAllRolePermission();

    @Operation(summary = "Gán thêm một quyền cho vai trò")
    @PostMapping
    @RequiresPermission(PermissionDefine.ROLE_PERMISSION_ASSIGN)
    ApiResponse<Void> assignPermission(@RequestParam String roleCode, @Valid @RequestBody AssignPermissionRequest request);

    @Operation(summary = "Cập nhật toàn bộ quyền của vai trò")
    @PutMapping
    @RequiresPermission(PermissionDefine.ROLE_PERMISSION_UPDATE)
    ApiResponse<Void> updatePermissions(@RequestParam String roleCode,
                                         @Valid @RequestBody UpdateRolePermissionsRequest request);

    @Operation(summary = "Xóa một quyền khỏi vai trò")
    @DeleteMapping
    @RequiresPermission(PermissionDefine.ROLE_PERMISSION_DELETE)
    ApiResponse<Void> deletePermission(@RequestParam String roleCode, @RequestParam String permissionCode);
}
