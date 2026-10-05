package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.controller.api.UserApi;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController implements UserApi {

    ApiResponseFactory apiResponseFactory;
    UsersService usersService;


    @Override
    public ApiResponse<PageResponse<UserResponse>> getUsers(){
        return apiResponseFactory.success(usersService.getUsers());
    }

    @Override
    public ApiResponse<UserResponse> updateUserStatus(String userName, UsersEnum action) {
        return apiResponseFactory.success(usersService.updateUserStatus(userName, action));
    }
}
