package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.RoleCreateRequest;
import com.example.employee_service.feature.model.request.RoleSearchRequest;
import com.example.employee_service.feature.model.request.RoleUpdateRequest;
import com.example.employee_service.feature.model.response.PositionResponse;
import com.example.employee_service.feature.model.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/role")
public interface RoleApi {
    @Operation(summary = "Tìm kiếm vai trò có phân trang")
    @PostMapping("/search")
    ApiResponse<PageResponse<RoleResponse>> searchRoles(@Valid @RequestBody RoleSearchRequest request);

    @Operation(summary = "Xem chi tiết vai trò")
    @GetMapping("/{id}")
    ApiResponse<RoleResponse> getRole(@PathVariable Long id);

    @Operation(summary = "Thêm vai trò")
    @PostMapping
    ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request);

    @Operation(summary = "Sửa vai trò")
    @PutMapping("/{id}")
    ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request);

    @Operation(summary = "Xóa mềm vai trò")
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteRole(@PathVariable Long id);

    @Operation(summary = "Lấy danh sách chức danh")
    @GetMapping("/positions")
    ApiResponse<List<PositionResponse>> getPositions();
}
