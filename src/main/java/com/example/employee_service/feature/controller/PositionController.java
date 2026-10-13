package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.controller.api.PositionApi;
import com.example.employee_service.feature.model.request.PositionCreateRequest;
import com.example.employee_service.feature.model.request.PositionSearchRequest;
import com.example.employee_service.feature.model.request.PositionUpdateRequest;
import com.example.employee_service.feature.model.response.PositionManagementResponse;
import com.example.employee_service.feature.service.PositionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PositionController implements PositionApi {
    PositionService positionService;
    ApiResponseFactory apiResponseFactory;

    @Override
    public ApiResponse<PageResponse<PositionManagementResponse>> searchPositions(PositionSearchRequest request) {
        return apiResponseFactory.success(positionService.searchPositions(request));
    }

    @Override
    public ApiResponse<PositionManagementResponse> getPosition(Long id) {
        return apiResponseFactory.success(positionService.getPosition(id));
    }

    @Override
    public ApiResponse<PositionManagementResponse> createPosition(PositionCreateRequest request) {
        return apiResponseFactory.success(positionService.createPosition(request));
    }

    @Override
    public ApiResponse<PositionManagementResponse> updatePosition(Long id, PositionUpdateRequest request) {
        return apiResponseFactory.success(positionService.updatePosition(id, request));
    }

    @Override
    public ApiResponse<Void> deletePosition(Long id) {
        positionService.deletePosition(id);
        return apiResponseFactory.success();
    }
}
