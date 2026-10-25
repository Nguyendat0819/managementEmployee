package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.request.AdminPasswordResetRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.model.response.PasswordResetRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "User API", description = "Quản lý người dùng")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Lấy thông tin tài khoản")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PageResponse<UserResponse>> getUsers();

    @Operation(summary = "Lấy các yêu cầu đặt lại mật khẩu đang chờ")
    @GetMapping("/password-reset-requests")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<PasswordResetRequestResponse>> getPasswordResetRequests();

    @Operation(summary = "Tạo tài khoản người dùng")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request);

    @Operation(summary = "Thay đổi trạng thái tài khoản")
    @PostMapping("/{userName}/status")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<UserResponse> updateUserStatus(@PathVariable String userName, @RequestParam UsersEnum action);

    @Operation(summary = "Quản trị viên đặt mật khẩu mới cho tài khoản")
    @PutMapping("/{userName}/password")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> resetUserPassword(@PathVariable String userName, @Valid @RequestBody AdminPasswordResetRequest request);

}
