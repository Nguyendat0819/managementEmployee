package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.feature.controller.api.UserApi;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.service.KeycloakService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController implements UserApi {

    ApiResponseFactory apiResponseFactory;
    KeycloakService keycloakService;

    @Override
    public ApiResponse<String> createUser(UserCreateRequest request) {
        String keycloakUserId = keycloakService.createUser(request);
        return apiResponseFactory.success(keycloakUserId);
    }
}
