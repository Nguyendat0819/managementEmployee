package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "Quản lý người dùng")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Lấy thông tin tài khoản")
    @GetMapping
    @RequiresPermission(PermissionDefine.USER_READ)
    ApiResponse<PageResponse<UserResponse>> getUsers();

    @Operation(summary = "Thay đổi trạng thái tài khoản")
    @PostMapping("/{userName}/status")
    @RequiresPermission(PermissionDefine.USER_STATUS_UPDATE)
    ApiResponse<UserResponse> updateUserStatus(@PathVariable String userName, @RequestParam UsersEnum action);

}
