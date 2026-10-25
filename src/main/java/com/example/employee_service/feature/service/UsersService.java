package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.response.UserResponse;

public interface UsersService {

    PageResponse<UserResponse> getUsers();

}
