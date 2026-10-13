package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.model.response.EmployeeCatalogResponse;
import com.example.employee_service.feature.model.request.EmployeeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/employee")
public interface EmployeeApi {
    @Operation(summary = "Lấy hồ sơ nhân viên hiện tại")
    @GetMapping({"/me", "/profile-me"})
    ApiResponse<EmployeeResponse> getCurrentEmployee(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Lấy danh mục dùng cho form nhân viên")
    @GetMapping("/catalogs")
    @RequiresPermission(PermissionDefine.EMPLOYEE_READ)
    ApiResponse<EmployeeCatalogResponse> getCatalogs();

    @Operation(summary = "Lấy danh sách nhân viên")
    @GetMapping
    @RequiresPermission(PermissionDefine.EMPLOYEE_READ)
    ApiResponse<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employmentType);

    @Operation(summary = "Tìm kiếm nhân viên có phân trang")
    @PostMapping("/search")
    @RequiresPermission(PermissionDefine.EMPLOYEE_SEARCH)
    ApiResponse<PageResponse<EmployeeResponse>> searchEmployees(
            @Valid @RequestBody EmployeeSearchRequest request);

    @Operation(summary = "Xem chi tiết nhân viên")
    @GetMapping("/{id:\\d+}")
    @RequiresPermission(PermissionDefine.EMPLOYEE_READ)
    ApiResponse<EmployeeResponse> getEmployee(@PathVariable Long id);

    @Operation(summary = "Thêm nhân viên")
    @PostMapping
    @RequiresPermission(PermissionDefine.EMPLOYEE_CREATE)
    ApiResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request);

    @Operation(summary = "Sửa nhân viên")
    @PutMapping("/{id:\\d+}")
    @RequiresPermission(PermissionDefine.EMPLOYEE_UPDATE)
    ApiResponse<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request);

    @Operation(summary = "Xóa mềm nhân viên")
    @DeleteMapping("/{id:\\d+}")
    @RequiresPermission(PermissionDefine.EMPLOYEE_DELETE)
    ApiResponse<Void> deleteEmployee(@PathVariable Long id);

}
