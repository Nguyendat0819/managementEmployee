package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.model.request.EmployeeRequest;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Lấy danh sách nhân viên")
    @GetMapping
    ApiResponse<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employmentType);

    @Operation(summary = "Tìm kiếm nhân viên có phân trang")
    @PostMapping("/search")
    ApiResponse<PageResponse<EmployeeResponse>> searchEmployees(
            @Valid @RequestBody EmployeeSearchRequest request);

    @Operation(summary = "Xem chi tiết nhân viên")
    @GetMapping("/{id}")
    ApiResponse<EmployeeResponse> getEmployee(@PathVariable Long id);

    @Operation(summary = "Thêm nhân viên")
    @PostMapping
    ApiResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request);

    @Operation(summary = "Sửa nhân viên")
    @PutMapping("/{id}")
    ApiResponse<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request);

    @Operation(summary = "Xóa mềm nhân viên")
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteEmployee(@PathVariable Long id);

    @GetMapping("/test")
    ApiResponse<String> test();
}
