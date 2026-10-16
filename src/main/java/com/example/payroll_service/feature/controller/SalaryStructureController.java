package com.example.payroll_service.feature.controller;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.ApiResponseFactory;
import com.example.payroll_service.feature.controller.api.SalaryStructureApi;
import com.example.payroll_service.feature.model.request.SalaryStructureRequest;
import com.example.payroll_service.feature.model.response.SalaryStructureResponse;
import com.example.payroll_service.feature.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SalaryStructureController implements SalaryStructureApi {
    private final SalaryStructureService service;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<SalaryStructureResponse>> getAll(String search) {
        return responseFactory.success(service.getAll(search));
    }

    @Override
    public ApiResponse<SalaryStructureResponse> getById(Long id) {
        return responseFactory.success(service.getById(id));
    }

    @Override
    public ApiResponse<SalaryStructureResponse> create(SalaryStructureRequest request) {
        return responseFactory.success(service.save(null, request));
    }

    @Override
    public ApiResponse<SalaryStructureResponse> update(Long id, SalaryStructureRequest request) {
        return responseFactory.success(service.save(id, request));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        service.delete(id);
        return responseFactory.success();
    }
}
