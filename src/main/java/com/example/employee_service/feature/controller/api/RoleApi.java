package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.response.PositionResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/role")
public interface RoleApi {
    @Operation(summary = "Lấy danh sách chức danh")
    @GetMapping("/positions")
    ApiResponse<List<PositionResponse>> getPositions();
}
