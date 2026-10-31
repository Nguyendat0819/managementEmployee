package com.example.attendance_service.feature.controller.api;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.common.security.RequiresPermission;
import com.example.attendance_service.feature.model.request.ShiftRequest;
import com.example.attendance_service.feature.model.response.ShiftResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/shifts")
public interface ShiftApi {
    @GetMapping
    @RequiresPermission("employee:shift:read")
    ApiResponse<List<ShiftResponse>> list();

    @PostMapping
    @RequiresPermission("employee:shift:manage")
    ApiResponse<ShiftResponse> create(@Valid @RequestBody ShiftRequest request);

    @PutMapping("/{id}")
    @RequiresPermission("employee:shift:manage")
    ApiResponse<ShiftResponse> update(@PathVariable Long id, @Valid @RequestBody ShiftRequest request);

    @DeleteMapping("/{id}")
    @RequiresPermission("employee:shift:manage")
    ApiResponse<Void> delete(@PathVariable Long id);
}
