package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.request.PositionCreateRequest;
import com.example.employee_service.feature.model.request.PositionSearchRequest;
import com.example.employee_service.feature.model.request.PositionUpdateRequest;
import com.example.employee_service.feature.model.response.PositionManagementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Position API", description = "Quản lý danh mục chức vụ")
@RequestMapping("/api/position")
public interface PositionApi {
    @Operation(summary = "Tìm kiếm chức vụ có phân trang")
    @PostMapping("/search")
    @RequiresPermission(PermissionDefine.POSITION_SEARCH)
    ApiResponse<PageResponse<PositionManagementResponse>> searchPositions(
            @Valid @RequestBody PositionSearchRequest request);

    @Operation(summary = "Xem chi tiết chức vụ")
    @GetMapping("/{id}")
    @RequiresPermission(PermissionDefine.POSITION_READ)
    ApiResponse<PositionManagementResponse> getPosition(@PathVariable Long id);

    @Operation(summary = "Thêm chức vụ")
    @PostMapping
    @RequiresPermission(PermissionDefine.POSITION_CREATE)
    ApiResponse<PositionManagementResponse> createPosition(@Valid @RequestBody PositionCreateRequest request);

    @Operation(summary = "Sửa chức vụ")
    @PutMapping("/{id}")
    @RequiresPermission(PermissionDefine.POSITION_UPDATE)
    ApiResponse<PositionManagementResponse> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionUpdateRequest request);

    @Operation(summary = "Xóa mềm chức vụ")
    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionDefine.POSITION_DELETE)
    ApiResponse<Void> deletePosition(@PathVariable Long id);
}
