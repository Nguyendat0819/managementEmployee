package com.example.payroll_service.feature.controller;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.ApiResponseFactory;
import com.example.payroll_service.feature.controller.api.PayrollRunApi;
import com.example.payroll_service.feature.model.request.PayrollRunRequest;
import com.example.payroll_service.feature.model.request.PayslipBatchRequest;
import com.example.payroll_service.feature.model.response.PayrollRunResponse;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import com.example.payroll_service.feature.model.response.PayrollCalculationResponse;
import com.example.payroll_service.feature.service.PayrollRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayrollRunController implements PayrollRunApi {
    private final PayrollRunService service;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<PayrollRunResponse>> getAll() {
        return responseFactory.success(service.getAll());
    }

    @Override
    public ApiResponse<PayrollRunResponse> getById(Long id) {
        return responseFactory.success(service.getById(id));
    }

    @Override
    public ApiResponse<PayrollRunResponse> create(PayrollRunRequest request) {
        return responseFactory.success(service.create(request));
    }

    @Override
    public ApiResponse<Void> deleteDraft(Long id) {
        service.deleteDraft(id);
        return responseFactory.success();
    }

    @Override
    public ApiResponse<List<PayslipResponse>> addPayslips(Long id, PayslipBatchRequest request) {
        return responseFactory.success(service.addPayslips(id, request.entries()));
    }

    @Override
    public ApiResponse<List<PayslipResponse>> getPayslips(Long id) {
        return responseFactory.success(service.getPayslips(id));
    }

    @Override
    public ApiResponse<PayrollCalculationResponse> calculate(Long id) {
        return responseFactory.success(service.calculate(id));
    }

    @Override
    public ApiResponse<PayrollRunResponse> submitForApproval(Long id) {
        return responseFactory.success(service.submitForApproval(id));
    }

    @Override
    public ApiResponse<PayrollRunResponse> approve(Long id) {
        return responseFactory.success(service.approve(id));
    }

    @Override
    public ApiResponse<PayrollRunResponse> markPaid(Long id) {
        return responseFactory.success(service.markPaid(id));
    }
}
