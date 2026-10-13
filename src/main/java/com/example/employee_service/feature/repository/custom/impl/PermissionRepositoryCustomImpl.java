package com.example.employee_service.feature.repository.custom.impl;

import com.example.employee_service.common.persistence.NativeQuerySupport;
import com.example.employee_service.feature.model.request.PermissionSearchRequest;
import com.example.employee_service.feature.model.response.PermissionResponse;
import com.example.employee_service.feature.repository.custom.PermissionRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PermissionRepositoryCustomImpl extends NativeQuerySupport implements PermissionRepositoryCustom {
    private static final String SELECT_SQL = """
            SELECT p.id,
                   p.permission_code AS permission_code,
                   p.permission_name AS permission_name,
                   p.description,
                   p.is_deleted AS is_deleted,
                   p.created_at AS created_at,
                   p.updated_at AS updated_at
              FROM hr_service.permissions p
             WHERE 1 = 1
            """;

    @Override
    public Page<PermissionResponse> search(PermissionSearchRequest request) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(request.getSearch())) {
            sql.append(" AND (p.permission_code ILIKE :search OR p.permission_name ILIKE :search OR p.description ILIKE :search)");
            params.put("search", appendLikeEscapee(request.getSearch()));
        }
        if (request.getIsDeleted() != null) {
            sql.append(" AND p.is_deleted = :isDeleted");
            params.put("isDeleted", request.getIsDeleted());
        }

        return getListPagination(sql.toString(), params, request, PermissionResponse.class,
                "permission_name ASC, permission_code ASC, id ASC");
    }
}
