package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.response.AuthResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {
    AuthResponse getCurrentUser(Jwt jwt);
}