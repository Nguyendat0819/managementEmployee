package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    AuthResponse getCurrentUser(Jwt jwt);
}
