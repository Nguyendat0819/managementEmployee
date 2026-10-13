package com.example.employee_service.feature.repository.custom;

import com.example.employee_service.feature.model.request.PositionSearchRequest;
import com.example.employee_service.feature.model.response.PositionManagementResponse;
import org.springframework.data.domain.Page;

public interface PositionRepositoryCustom {
    Page<PositionManagementResponse> search(PositionSearchRequest request);
}
