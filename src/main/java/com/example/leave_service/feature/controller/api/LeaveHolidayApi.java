package com.example.leave_service.feature.controller.api;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.security.RequiresPermission;
import com.example.leave_service.feature.model.request.SaveHolidayRequest;
import com.example.leave_service.feature.model.response.HolidayResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/holidays")
public interface LeaveHolidayApi {

    @GetMapping
    @RequiresPermission("employee:leave:read")
    ApiResponse<List<HolidayResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);

    @PostMapping
    @RequiresPermission("employee:leave:manage")
    ApiResponse<HolidayResponse> create(@Valid @RequestBody SaveHolidayRequest request);

    @PutMapping("/{id}")
    @RequiresPermission("employee:leave:manage")
    ApiResponse<HolidayResponse> update(@PathVariable Long id, @Valid @RequestBody SaveHolidayRequest request);

    @DeleteMapping("/{id}")
    @RequiresPermission("employee:leave:manage")
    ApiResponse<Void> delete(@PathVariable Long id);
}
