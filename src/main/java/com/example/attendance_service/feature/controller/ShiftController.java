package com.example.attendance_service.feature.controller;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.common.response.ApiResponseFactory;
import com.example.attendance_service.feature.controller.api.ShiftApi;
import com.example.attendance_service.feature.model.request.ShiftRequest;
import com.example.attendance_service.feature.model.response.ShiftResponse;
import com.example.attendance_service.feature.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShiftController implements ShiftApi {
    private final ShiftService shiftService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<ShiftResponse>> list() {
        return responseFactory.success(shiftService.list());
    }

    @Override
    public ApiResponse<ShiftResponse> create(ShiftRequest request) {
        return responseFactory.success(shiftService.create(request));
    }

    @Override
    public ApiResponse<ShiftResponse> update(Long id, ShiftRequest request) {
        return responseFactory.success(shiftService.update(id, request));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        shiftService.delete(id);
        return responseFactory.success();
    }
}
