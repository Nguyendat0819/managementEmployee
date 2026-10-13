package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.PositionCreateRequest;
import com.example.employee_service.feature.model.request.PositionSearchRequest;
import com.example.employee_service.feature.model.request.PositionUpdateRequest;
import com.example.employee_service.feature.model.response.PositionManagementResponse;

public interface PositionService {
    PageResponse<PositionManagementResponse> searchPositions(PositionSearchRequest request);

    PositionManagementResponse getPosition(Long id);

    PositionManagementResponse createPosition(PositionCreateRequest request);

    PositionManagementResponse updatePosition(Long id, PositionUpdateRequest request);

    void deletePosition(Long id);
}
