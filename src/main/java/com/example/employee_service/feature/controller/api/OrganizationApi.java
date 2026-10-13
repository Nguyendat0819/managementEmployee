package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.request.DepartmentsRequest;
import com.example.employee_service.feature.model.request.DepartmentsUpdateRequest;
import com.example.employee_service.feature.model.response.DepartmentResponse;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Organization API", description = "Cơ cấu tổ chức và nhân sự theo phòng ban")
@RequestMapping("/api/departments")
public interface OrganizationApi {
    @Operation(summary = "Thêm mới phòng ban")
    @PostMapping
    ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentsRequest request);

    @Operation(summary = "Cập nhật phòng ban")
    @PutMapping("/{deptCode}")
    ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable("deptCode") @NotBlank(message = "Mã phòng ban không được để trống") String deptCode,
            @Valid @RequestBody DepartmentsUpdateRequest request
    );

    @Operation(summary = "Xóa mềm phòng ban")
    @DeleteMapping("/{deptCode}")
    ApiResponse<Void> deleteDepartment(
            @PathVariable("deptCode") @NotBlank(message = "Mã phòng ban không được để trống") String deptCode
    );

    @Operation(summary = "Lấy danh sách phòng ban")
    @GetMapping
    ApiResponse<List<DepartmentResponse>> getDepartments(
            @RequestParam(name = "search", required = false)
            @Size(max = 100, message = "Từ khóa tối đa 100 ký tự") String search,
            @RequestParam(name = "level", required = false)
            @Min(value = 1, message = "Cấp tổ chức phải lớn hơn hoặc bằng 1") Short level,
            @RequestParam(name = "parentCode", required = false)
            @Size(max = 255, message = "Mã đơn vị cha tối đa 255 ký tự") String parentCode,
            @RequestParam(name = "isDeleted", required = false) Boolean isDeleted
    );

    @Operation(summary = "Lấy danh sách nhân sự theo phòng ban")
    @GetMapping("/{departmentCode}/employees")
    ApiResponse<List<EmployeeResponse>> getEmployeesByDepartment(
            @PathVariable("departmentCode") @NotBlank(message = "Mã phòng ban không được để trống") String departmentCode
    );
}
