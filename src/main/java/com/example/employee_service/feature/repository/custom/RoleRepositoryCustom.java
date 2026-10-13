package com.example.employee_service.feature.repository.custom;

import com.example.employee_service.feature.model.request.RoleSearchRequest;
import com.example.employee_service.feature.model.response.RoleResponse;
import org.springframework.data.domain.Page;

public interface RoleRepositoryCustom {
    Page<RoleResponse> search(RoleSearchRequest request);
}
