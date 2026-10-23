package com.example.leave_service.feature.controller;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.response.ApiResponseFactory;
import com.example.leave_service.common.response.PageResponse;
import com.example.leave_service.common.util.SecurityUtil;
import com.example.leave_service.feature.controller.api.LeaveRequestApi;
import com.example.leave_service.feature.model.request.CreateLeaveRequestRequest;
import com.example.leave_service.feature.model.request.LeaveDecisionRequest;
import com.example.leave_service.feature.model.response.LeaveRequestResponse;
import com.example.leave_service.feature.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaveRequestController implements LeaveRequestApi {

    private final LeaveRequestService leaveRequestService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<LeaveRequestResponse> create(CreateLeaveRequestRequest request) {
        return responseFactory.success(leaveRequestService.create(request, SecurityUtil.getCurrentUserId()));
    }

    @Override
    public ApiResponse<List<LeaveRequestResponse>> getMine() {
        return responseFactory.success(leaveRequestService.getMine(SecurityUtil.getCurrentUserId()));
    }

    @Override
    public ApiResponse<LeaveRequestResponse> cancel(Long id) {
        return responseFactory.success(leaveRequestService.cancel(id, SecurityUtil.getCurrentUserId()));
    }

    @Override
    public ApiResponse<PageResponse<LeaveRequestResponse>> search(String status, String employeeCode,
                                                                  LocalDate from, LocalDate to,
                                                                  int page, int size) {
        return responseFactory.success(leaveRequestService.search(status, employeeCode, from, to, page, size));
    }

    @Override
    public ApiResponse<LeaveRequestResponse> approve(Long id, LeaveDecisionRequest request) {
        return responseFactory.success(leaveRequestService.approve(id, request));
    }

    @Override
    public ApiResponse<LeaveRequestResponse> reject(Long id, LeaveDecisionRequest request) {
        return responseFactory.success(leaveRequestService.reject(id, request));
    }
}
