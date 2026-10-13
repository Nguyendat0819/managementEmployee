package com.example.employee_service.feature.repository.custom;

import com.example.employee_service.feature.model.request.PermissionSearchRequest;
import com.example.employee_service.feature.model.response.PermissionResponse;
import org.springframework.data.domain.Page;

public interface PermissionRepositoryCustom {
    Page<PermissionResponse> search(PermissionSearchRequest request);
}
