package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.feature.controller.api.AuthApi;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.request.RefreshTokenRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final ApiResponseFactory apiResponseFactory;
    private final AuthService authService;

    @Override
    public ApiResponse<TokenResponse> login(LoginRequest request) {
        return apiResponseFactory.success(authService.login(request));
    }

    @Override
    public ApiResponse<TokenResponse> refresh(RefreshTokenRequest request) {
        return apiResponseFactory.success(authService.refresh(request));
    }

    @Override
    public ApiResponse<Void> logout(RefreshTokenRequest request) {
        authService.logout(request);
        return apiResponseFactory.success();
    }

    @Override
    public ApiResponse<AuthResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return apiResponseFactory.success(authService.getCurrentUser(jwt));
    }

    @Override
    public ApiResponse<String> createUser(UserCreateRequest request) {
        // AuthController chỉ điều phối request; nghiệp vụ tạo user, mã hóa password
        // và lưu database được thực hiện ở AuthService.
        UserResponse userResponse = authService.create(request);
        // Chỉ trả username làm kết quả xác nhận, không trả entity chứa password hoặc audit data.
        return apiResponseFactory.success(userResponse.getUserName());
    }
}
