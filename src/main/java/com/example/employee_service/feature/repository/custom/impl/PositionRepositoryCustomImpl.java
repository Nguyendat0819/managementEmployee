package com.example.employee_service.feature.repository.custom.impl;

import com.example.employee_service.common.persistence.NativeQuerySupport;
import com.example.employee_service.feature.model.request.PositionSearchRequest;
import com.example.employee_service.feature.model.response.PositionManagementResponse;
import com.example.employee_service.feature.repository.custom.PositionRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PositionRepositoryCustomImpl extends NativeQuerySupport implements PositionRepositoryCustom {
    private static final String SELECT_SQL = """
            SELECT p.id,
                   p.position_code AS position_code,
                   p.title,
                   p.level,
                   p.base_salary_min AS base_salary_min,
                   p.base_salary_max AS base_salary_max,
                   (SELECT COUNT(*)
                      FROM hr_service.employees e
                     WHERE e.position_code = p.position_code
                       AND e.is_deleted = FALSE) AS employee_count,
                   p.is_deleted AS is_deleted,
                   p.created_at AS created_at,
                   p.updated_at AS updated_at
              FROM hr_service.positions p
             WHERE 1 = 1
            """;

    @Override
    public Page<PositionManagementResponse> search(PositionSearchRequest request) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(request.getSearch())) {
            sql.append("""
                     AND (p.position_code ILIKE :search
                          OR p.title ILIKE :search
                          OR p.level ILIKE :search)
                    """);
            params.put("search", appendLikeEscapee(request.getSearch()));
        }
        if (StringUtils.isNotBlank(request.getPositionCode())) {
            sql.append(" AND p.position_code = :positionCode");
            params.put("positionCode", request.getPositionCode());
        }
        if (StringUtils.isNotBlank(request.getLevel())) {
            sql.append(" AND p.level = :level");
            params.put("level", request.getLevel());
        }
        if (request.getIsDeleted() != null) {
            sql.append(" AND p.is_deleted = :isDeleted");
            params.put("isDeleted", request.getIsDeleted());
        }

        return getListPagination(
                sql.toString(),
                params,
                request,
                PositionManagementResponse.class,
                "title ASC, id ASC"
        );
    }
}
