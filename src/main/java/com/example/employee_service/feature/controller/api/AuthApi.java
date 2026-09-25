package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AuthApi {
    @Operation(summary = "Lấy thông tin người dùng")
    @GetMapping("/me")
    ApiResponse<AuthResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt);
}