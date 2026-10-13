package com.example.employee_service.feature.repository.custom.impl;

import com.example.employee_service.common.persistence.NativeQuerySupport;
import com.example.employee_service.feature.model.request.RoleSearchRequest;
import com.example.employee_service.feature.model.response.RoleResponse;
import com.example.employee_service.feature.repository.custom.RoleRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class RoleRepositoryCustomImpl extends NativeQuerySupport implements RoleRepositoryCustom {
    private static final String SELECT_SQL = """
            SELECT r.id,
                   r.role_code AS role_code,
                   r.role_name AS role_name,
                   r.description,
                   (SELECT COUNT(*)
                      FROM hr_service.users u
                     WHERE u.role_code = r.role_code
                       AND u.is_deleted = FALSE) AS user_count,
                   (SELECT COUNT(*)
                      FROM hr_service.role_permissions rp
                     WHERE rp.role_code = r.role_code
                       AND rp.is_deleted = FALSE) AS permission_count,
                   r.is_deleted AS is_deleted,
                   r.created_at AS created_at,
                   r.updated_at AS updated_at
              FROM hr_service.roles r
             WHERE 1 = 1
            """;

    @Override
    public Page<RoleResponse> search(RoleSearchRequest request) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(request.getSearch())) {
            sql.append("""
                     AND (r.role_code ILIKE :search
                          OR r.role_name ILIKE :search
                          OR r.description ILIKE :search)
                    """);
            params.put("search", appendLikeEscapee(request.getSearch()));
        }
        if (StringUtils.isNotBlank(request.getRoleCode())) {
            sql.append(" AND r.role_code = :roleCode");
            params.put("roleCode", request.getRoleCode());
        }
        if (request.getIsDeleted() != null) {
            sql.append(" AND r.is_deleted = :isDeleted");
            params.put("isDeleted", request.getIsDeleted());
        }

        return getListPagination(
                sql.toString(),
                params,
                request,
                RoleResponse.class,
                "role_name ASC, id ASC"
        );
    }
}
