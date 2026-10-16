package com.example.payroll_service.feature.controller.api;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.security.RequiresPermission;
import com.example.payroll_service.feature.model.request.PayrollRunRequest;
import com.example.payroll_service.feature.model.request.PayslipBatchRequest;
import com.example.payroll_service.feature.model.response.PayrollRunResponse;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/payroll-runs")
public interface PayrollRunApi {
    @GetMapping
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<List<PayrollRunResponse>> getAll();

    @GetMapping("/{id}")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<PayrollRunResponse> getById(@PathVariable Long id);

    @PostMapping
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<PayrollRunResponse> create(@Valid @RequestBody PayrollRunRequest request);

    @DeleteMapping("/{id}")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<Void> deleteDraft(@PathVariable Long id);

    @PostMapping("/{id}/payslips")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<List<PayslipResponse>> addPayslips(@PathVariable Long id, @Valid @RequestBody PayslipBatchRequest request);

    @GetMapping("/{id}/payslips")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<List<PayslipResponse>> getPayslips(@PathVariable Long id);

    @PostMapping("/{id}/submit")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<PayrollRunResponse> submitForApproval(@PathVariable Long id);

    @PostMapping("/{id}/approve")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<PayrollRunResponse> approve(@PathVariable Long id);

    @PostMapping("/{id}/mark-paid")
    @RequiresPermission("employee:payroll:manage")
    ApiResponse<PayrollRunResponse> markPaid(@PathVariable Long id);
}
