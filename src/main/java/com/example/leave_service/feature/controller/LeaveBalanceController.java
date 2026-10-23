package com.example.leave_service.feature.controller;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.response.ApiResponseFactory;
import com.example.leave_service.common.util.SecurityUtil;
import com.example.leave_service.feature.controller.api.LeaveBalanceApi;
import com.example.leave_service.feature.model.response.LeaveBalanceResponse;
import com.example.leave_service.feature.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaveBalanceController implements LeaveBalanceApi {

    private final LeaveRequestService leaveRequestService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<LeaveBalanceResponse>> getMine(Integer year) {
        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        return responseFactory.success(leaveRequestService.getMyBalances(SecurityUtil.getCurrentUserId(), selectedYear));
    }
}
