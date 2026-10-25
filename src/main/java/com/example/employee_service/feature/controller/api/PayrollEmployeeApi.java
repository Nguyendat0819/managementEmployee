package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.model.response.PayrollEmployeeResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/internal/payroll")
public interface PayrollEmployeeApi {
    @GetMapping("/employees")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<List<PayrollEmployeeResponse>> getEligibleEmployees(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);
}
