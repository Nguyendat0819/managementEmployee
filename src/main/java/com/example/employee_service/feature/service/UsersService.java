package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;

public interface UsersService {

    PageResponse<UserResponse> getUsers();

    UserResponse updateUserStatus(String userName, UsersEnum action);
}
