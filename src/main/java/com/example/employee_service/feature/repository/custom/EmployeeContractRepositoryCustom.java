package com.example.employee_service.feature.repository.custom;

import com.example.employee_service.feature.model.request.EmployeeContractSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeContractResponse;
import org.springframework.data.domain.Page;

public interface EmployeeContractRepositoryCustom {
    Page<EmployeeContractResponse> search(EmployeeContractSearchRequest request);
}
