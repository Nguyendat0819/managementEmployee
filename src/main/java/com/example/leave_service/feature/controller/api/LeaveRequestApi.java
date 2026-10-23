package com.example.leave_service.feature.controller.api;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.response.PageResponse;
import com.example.leave_service.common.security.RequiresPermission;
import com.example.leave_service.feature.model.request.CreateLeaveRequestRequest;
import com.example.leave_service.feature.model.request.LeaveDecisionRequest;
import com.example.leave_service.feature.model.response.LeaveRequestResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/leave-requests")
public interface LeaveRequestApi {

    @PostMapping
    @RequiresPermission("employee:leave:read")
    ApiResponse<LeaveRequestResponse> create(@Valid @RequestBody CreateLeaveRequestRequest request);

    @GetMapping("/me")
    @RequiresPermission("employee:leave:read")
    ApiResponse<List<LeaveRequestResponse>> getMine();

    @PatchMapping("/me/{id}/cancel")
    @RequiresPermission("employee:leave:read")
    ApiResponse<LeaveRequestResponse> cancel(@PathVariable Long id);

    @GetMapping
    @RequiresPermission("employee:leave:approve")
    ApiResponse<PageResponse<LeaveRequestResponse>> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @PatchMapping("/{id}/approve")
    @RequiresPermission("employee:leave:approve")
    ApiResponse<LeaveRequestResponse> approve(@PathVariable Long id,
                                              @Valid @RequestBody(required = false) LeaveDecisionRequest request);

    @PatchMapping("/{id}/reject")
    @RequiresPermission("employee:leave:approve")
    ApiResponse<LeaveRequestResponse> reject(@PathVariable Long id,
                                             @Valid @RequestBody LeaveDecisionRequest request);
}
