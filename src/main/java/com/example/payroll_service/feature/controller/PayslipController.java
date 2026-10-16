package com.example.payroll_service.feature.controller;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.ApiResponseFactory;
import com.example.payroll_service.feature.controller.api.PayslipApi;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import com.example.payroll_service.feature.service.PayslipQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayslipController implements PayslipApi {
    private final PayslipQueryService service;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<PayslipResponse>> getCurrentEmployeePayslips() {
        return responseFactory.success(service.getCurrentEmployeePayslips());
    }

    @Override
    public ApiResponse<PayslipResponse> getCurrentEmployeePayslip(Long id) {
        return responseFactory.success(service.getCurrentEmployeePayslip(id));
    }
}
