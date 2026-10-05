package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.request.RefreshTokenRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
    AuthResponse getCurrentUser(Jwt jwt);
    UserResponse create(UserCreateRequest request);
}
