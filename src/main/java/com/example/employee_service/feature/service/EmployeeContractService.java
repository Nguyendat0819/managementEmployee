package com.example.employee_service.feature.service;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.model.request.EmployeeContractCreateRequest;
import com.example.employee_service.feature.model.request.EmployeeContractSearchRequest;
import com.example.employee_service.feature.model.request.EmployeeContractUpdateRequest;
import com.example.employee_service.feature.model.response.EmployeeContractResponse;

public interface EmployeeContractService {
    PageResponse<EmployeeContractResponse> searchContracts(EmployeeContractSearchRequest request);

    EmployeeContractResponse getContract(Long id);

    EmployeeContractResponse createContract(EmployeeContractCreateRequest request);

    EmployeeContractResponse updateContract(Long id, EmployeeContractUpdateRequest request);

    void deleteContract(Long id);
}
