package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.request.EmployeeContractCreateRequest;
import com.example.employee_service.feature.model.request.EmployeeContractSearchRequest;
import com.example.employee_service.feature.model.request.EmployeeContractUpdateRequest;
import com.example.employee_service.feature.model.response.EmployeeContractResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Employee Contract API", description = "Quản lý hợp đồng lao động")
@RequestMapping("/api/employee-contract")
public interface EmployeeContractApi {

    @Operation(summary = "Lấy hợp đồng hiện tại của tài khoản đăng nhập")
    @GetMapping("/contract-me")
    ApiResponse<EmployeeContractResponse> getCurrentContract(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Tìm kiếm hợp đồng lao động có phân trang")
    @PostMapping("/search")
    @RequiresPermission(PermissionDefine.CONTRACT_SEARCH)
    ApiResponse<PageResponse<EmployeeContractResponse>> searchContracts(
            @Valid @RequestBody EmployeeContractSearchRequest request);

    @Operation(summary = "Xem chi tiết hợp đồng lao động")
    @GetMapping("/{id:\\d+}")
    @RequiresPermission(PermissionDefine.CONTRACT_READ)
    ApiResponse<EmployeeContractResponse> getContract(@PathVariable Long id);

    @Operation(summary = "Thêm hợp đồng lao động")
    @PostMapping
    @RequiresPermission(PermissionDefine.CONTRACT_CREATE)
    ApiResponse<EmployeeContractResponse> createContract(
            @Valid @RequestBody EmployeeContractCreateRequest request);

    @Operation(summary = "Sửa hợp đồng lao động")
    @PutMapping("/{id:\\d+}")
    @RequiresPermission(PermissionDefine.CONTRACT_UPDATE)
    ApiResponse<EmployeeContractResponse> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeContractUpdateRequest request);

    @Operation(summary = "Xóa mềm hợp đồng lao động")
    @DeleteMapping("/{id:\\d+}")
    @RequiresPermission(PermissionDefine.CONTRACT_DELETE)
    ApiResponse<Void> deleteContract(@PathVariable Long id);
}
