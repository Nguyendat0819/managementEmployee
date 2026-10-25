package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.AuthResponse;
import com.example.employee_service.feature.model.response.TokenResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.model.request.RefreshTokenRequest;
import com.example.employee_service.feature.model.request.ChangePasswordRequest;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.model.response.PasswordResetRequestResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
    AuthResponse getCurrentUser(Jwt jwt);
    UserResponse create(UserCreateRequest request);
    UserResponse register(UserCreateRequest request);
    void changePassword(Jwt jwt, ChangePasswordRequest request);
    void adminResetPassword(String userName, String newPassword);
    UserResponse adminUpdateUserStatus(String userName, UsersEnum action);
    void requestAdminPasswordReset(String email);
    List<PasswordResetRequestResponse> getPasswordResetRequests();
}
