package com.example.attendance_service.feature.controller;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.common.response.ApiResponseFactory;
import com.example.attendance_service.feature.controller.api.ShiftAssignmentApi;
import com.example.attendance_service.feature.model.request.ShiftAssignmentRequest;
import com.example.attendance_service.feature.model.response.ShiftAssignmentResponse;
import com.example.attendance_service.feature.service.ShiftAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ShiftAssignmentController implements ShiftAssignmentApi {
    private final ShiftAssignmentService assignmentService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<ShiftAssignmentResponse>> list() {
        return responseFactory.success(assignmentService.list());
    }

    @Override
    public ApiResponse<ShiftAssignmentResponse> getCurrent(String employeeCode, LocalDate date) {
        return responseFactory.success(assignmentService.getCurrent(employeeCode, date));
    }

    @Override
    public ApiResponse<ShiftAssignmentResponse> create(ShiftAssignmentRequest request) {
        return responseFactory.success(assignmentService.create(request));
    }

    @Override
    public ApiResponse<ShiftAssignmentResponse> update(Long id, ShiftAssignmentRequest request) {
        return responseFactory.success(assignmentService.update(id, request));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        assignmentService.delete(id);
        return responseFactory.success();
    }
}
