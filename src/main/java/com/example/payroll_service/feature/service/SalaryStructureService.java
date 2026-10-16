package com.example.payroll_service.feature.service;

import com.example.payroll_service.feature.model.request.SalaryStructureRequest;
import com.example.payroll_service.feature.model.response.SalaryStructureResponse;

import java.util.List;

public interface SalaryStructureService {
    List<SalaryStructureResponse> getAll(String search);

    SalaryStructureResponse getById(Long id);

    SalaryStructureResponse save(Long id, SalaryStructureRequest request);

    void delete(Long id);
}
