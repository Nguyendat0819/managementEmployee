package com.example.payroll_service.feature.controller;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.ApiResponseFactory;
import com.example.payroll_service.feature.controller.api.PayrollHolidayApi;
import com.example.payroll_service.feature.model.request.PayrollHolidayRequest;
import com.example.payroll_service.feature.model.response.PayrollHolidayResponse;
import com.example.payroll_service.feature.service.PayrollHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayrollHolidayController implements PayrollHolidayApi {
    private final PayrollHolidayService service;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<PayrollHolidayResponse>> getAll() {
        return responseFactory.success(service.getAll());
    }

    @Override
    public ApiResponse<PayrollHolidayResponse> create(PayrollHolidayRequest request) {
        return responseFactory.success(service.save(null, request));
    }

    @Override
    public ApiResponse<PayrollHolidayResponse> update(Long id, PayrollHolidayRequest request) {
        return responseFactory.success(service.save(id, request));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        service.delete(id);
        return responseFactory.success();
    }
}
