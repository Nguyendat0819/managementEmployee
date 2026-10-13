package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.request.PermissionCreateRequest;
import com.example.employee_service.feature.model.request.PermissionSearchRequest;
import com.example.employee_service.feature.model.request.PermissionUpdateRequest;
import com.example.employee_service.feature.model.response.PermissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/permission")
public interface PermissionApi {
    @Operation(summary = "Tìm kiếm danh mục quyền")
    @PostMapping("/search")
    @RequiresPermission(PermissionDefine.PERMISSION_READ)
    ApiResponse<PageResponse<PermissionResponse>> search(@Valid @RequestBody PermissionSearchRequest request);

    @Operation(summary = "Xem chi tiết quyền")
    @GetMapping("/{id}")
    @RequiresPermission(PermissionDefine.PERMISSION_READ)
    ApiResponse<PermissionResponse> getPermission(@PathVariable Long id);

    @Operation(summary = "Thêm quyền")
    @PostMapping
    @RequiresPermission(PermissionDefine.PERMISSION_CREATE)
    ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionCreateRequest request);

    @Operation(summary = "Sửa quyền")
    @PutMapping("/{id}")
    @RequiresPermission(PermissionDefine.PERMISSION_UPDATE)
    ApiResponse<PermissionResponse> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionUpdateRequest request);

    @Operation(summary = "Xóa mềm quyền")
    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionDefine.PERMISSION_DELETE)
    ApiResponse<Void> deletePermission(@PathVariable Long id);
}
