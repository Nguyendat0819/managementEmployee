package com.example.payroll_service.feature.service;

import com.example.payroll_service.feature.model.response.PayslipResponse;

import java.util.List;

public interface PayslipQueryService {
    List<PayslipResponse> getCurrentEmployeePayslips();

    PayslipResponse getCurrentEmployeePayslip(Long id);
}
