package com.example.payroll_service.feature.controller.api;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.security.RequiresPermission;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/payslips/me")
@RequiresPermission("employee:payroll:read")
public interface PayslipApi {
    @GetMapping
    ApiResponse<List<PayslipResponse>> getCurrentEmployeePayslips();

    @GetMapping("/{id}")
    ApiResponse<PayslipResponse> getCurrentEmployeePayslip(@PathVariable Long id);
}
