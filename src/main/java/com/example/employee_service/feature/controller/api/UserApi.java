package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "Quản lý người dùng")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Tạo người dùng trên Keycloak")
    @PostMapping
    ApiResponse<String> createUser(@Valid @RequestBody UserCreateRequest request);

    @Operation(summary = "Lấy thông tin tài khoản")
    @GetMapping
    ApiResponse<PageResponse<UserResponse>> getUsers();

    @Operation(summary = "Thay đổi trạng thái tài khoản")
    @PostMapping("/{userName}/status")
    ApiResponse<UserResponse> updateUserStatus(@PathVariable String userName, @RequestParam UsersEnum action);

}
