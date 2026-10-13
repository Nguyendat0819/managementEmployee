package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.response.PositionResponse;

import java.util.List;

public interface RoleService {
    List<PositionResponse> getPositions();
}
