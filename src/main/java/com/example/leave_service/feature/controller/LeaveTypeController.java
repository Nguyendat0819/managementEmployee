package com.example.leave_service.feature.controller;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.response.ApiResponseFactory;
import com.example.leave_service.feature.controller.api.LeaveTypeApi;
import com.example.leave_service.feature.model.request.SaveLeaveTypeRequest;
import com.example.leave_service.feature.model.response.LeaveTypeResponse;
import com.example.leave_service.feature.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaveTypeController implements LeaveTypeApi {

    private final LeaveTypeService leaveTypeService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<LeaveTypeResponse>> list() {
        return responseFactory.success(leaveTypeService.list(false));
    }

    @Override
    public ApiResponse<List<LeaveTypeResponse>> listAll() {
        return responseFactory.success(leaveTypeService.list(true));
    }

    @Override
    public ApiResponse<LeaveTypeResponse> create(SaveLeaveTypeRequest request) {
        return responseFactory.success(leaveTypeService.save(null, request));
    }

    @Override
    public ApiResponse<LeaveTypeResponse> update(Long id, SaveLeaveTypeRequest request) {
        return responseFactory.success(leaveTypeService.save(id, request));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        leaveTypeService.delete(id);
        return responseFactory.success();
    }
}
