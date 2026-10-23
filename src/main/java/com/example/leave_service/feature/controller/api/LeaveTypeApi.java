package com.example.leave_service.feature.controller.api;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.security.RequiresPermission;
import com.example.leave_service.feature.model.request.SaveLeaveTypeRequest;
import com.example.leave_service.feature.model.response.LeaveTypeResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/leave-types")
public interface LeaveTypeApi {

    @GetMapping
    @RequiresPermission("employee:leave:read")
    ApiResponse<List<LeaveTypeResponse>> list();

    @GetMapping("/all")
    @RequiresPermission("employee:leave:manage")
    ApiResponse<List<LeaveTypeResponse>> listAll();

    @PostMapping
    @RequiresPermission("employee:leave:manage")
    ApiResponse<LeaveTypeResponse> create(@Valid @RequestBody SaveLeaveTypeRequest request);

    @PutMapping("/{id}")
    @RequiresPermission("employee:leave:manage")
    ApiResponse<LeaveTypeResponse> update(@PathVariable Long id, @Valid @RequestBody SaveLeaveTypeRequest request);

    @DeleteMapping("/{id}")
    @RequiresPermission("employee:leave:manage")
    ApiResponse<Void> delete(@PathVariable Long id);
}
