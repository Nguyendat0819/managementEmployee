package com.example.leave_service.feature.controller.api;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.security.RequiresPermission;
import com.example.leave_service.feature.model.response.LeaveBalanceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/leave-balances")
public interface LeaveBalanceApi {

    @GetMapping("/me")
    @RequiresPermission("employee:leave:read")
    ApiResponse<List<LeaveBalanceResponse>> getMine(@RequestParam(required = false) Integer year);
}
