package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User API", description = "Quản lý người dùng")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Tạo người dùng trên Keycloak")
    @PostMapping
    ApiResponse<String> createUser(@Valid @RequestBody UserCreateRequest request);
}
