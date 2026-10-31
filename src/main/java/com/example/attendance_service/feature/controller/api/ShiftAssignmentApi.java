package com.example.attendance_service.feature.controller.api;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.common.security.RequiresPermission;
import com.example.attendance_service.feature.model.request.ShiftAssignmentRequest;
import com.example.attendance_service.feature.model.response.ShiftAssignmentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/shift-assignments")
public interface ShiftAssignmentApi {
    @GetMapping
    @RequiresPermission("employee:shift:manage")
    ApiResponse<List<ShiftAssignmentResponse>> list();

    @GetMapping("/current")
    @RequiresPermission("employee:shift:read")
    ApiResponse<ShiftAssignmentResponse> getCurrent(
            @RequestParam String employeeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @PostMapping
    @RequiresPermission("employee:shift:manage")
    ApiResponse<ShiftAssignmentResponse> create(@Valid @RequestBody ShiftAssignmentRequest request);

    @PutMapping("/{id}")
    @RequiresPermission("employee:shift:manage")
    ApiResponse<ShiftAssignmentResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody ShiftAssignmentRequest request);

    @DeleteMapping("/{id}")
    @RequiresPermission("employee:shift:manage")
    ApiResponse<Void> delete(@PathVariable Long id);
}
