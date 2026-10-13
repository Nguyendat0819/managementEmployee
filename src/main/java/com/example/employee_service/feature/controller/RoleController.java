package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.feature.controller.api.RoleApi;
import com.example.employee_service.feature.model.response.PositionResponse;
import com.example.employee_service.feature.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController implements RoleApi {
    RoleService roleService;
    ApiResponseFactory apiResponseFactory;

    @Override
    public ApiResponse<List<PositionResponse>> getPositions() {
        return apiResponseFactory.success(roleService.getPositions());
    }
}
