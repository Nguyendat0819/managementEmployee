package com.example.employee_service.feature.repository.custom;

import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import org.springframework.data.domain.Page;

public interface EmployeeRepositoryCustom {
    Page<EmployeeResponse> search(EmployeeSearchRequest request);
}
