package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AuthApi {
    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    ApiResponse<TokenResponse> login(@RequestBody LoginRequest request);

    @Operation(summary = "Lấy thông tin người dùng")
    @GetMapping("/me")
    ApiResponse<AuthResponse> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);
}
