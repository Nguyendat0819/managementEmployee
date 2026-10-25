package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.controller.api.UserApi;
import com.example.employee_service.feature.model.request.AdminPasswordResetRequest;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.model.response.PasswordResetRequestResponse;
import com.example.employee_service.feature.service.AuthService;
import com.example.employee_service.feature.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController implements UserApi {

    ApiResponseFactory apiResponseFactory;
    UsersService usersService;
    AuthService authService;


    @Override
    public ApiResponse<PageResponse<UserResponse>> getUsers(){
        return apiResponseFactory.success(usersService.getUsers());
    }

    @Override
    public ApiResponse<List<PasswordResetRequestResponse>> getPasswordResetRequests() {
        return apiResponseFactory.success(authService.getPasswordResetRequests());
    }

    @Override
    public ApiResponse<UserResponse> createUser(UserCreateRequest request) {
        return apiResponseFactory.success(authService.create(request));
    }

    @Override
    public ApiResponse<UserResponse> updateUserStatus(String userName, UsersEnum action) {
        return apiResponseFactory.success(authService.adminUpdateUserStatus(userName, action));
    }

    @Override
    public ApiResponse<Void> resetUserPassword(String userName, AdminPasswordResetRequest request) {
        authService.adminResetPassword(userName, request.getNewPassword());
        return apiResponseFactory.success();
    }
}
