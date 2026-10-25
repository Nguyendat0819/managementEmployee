package com.example.employee_service.feature.repository.custom.impl;

import com.example.employee_service.common.persistence.NativeQuerySupport;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.repository.custom.EmployeeRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Repository
public class EmployeeRepositoryCustomImpl extends NativeQuerySupport implements EmployeeRepositoryCustom {
    private static final String SELECT_SQL = """
            SELECT e.id,
                   e.employee_code AS "employeeCode",
                   e.user_id AS "user",
                   e.full_name AS "fullName",
                   e.birth_date AS "birthDate",
                   e.gender,
                   e.id_card AS "idCard",
                   e.phone,
                   e.email,
                   e.department_code AS "departmentCode",
                   (SELECT d.name
                      FROM hr_service.departments d
                     WHERE d.dept_code = e.department_code
                       AND d.is_deleted = FALSE) AS "departmentName",
                   e.position_code AS "positionCode",
                   (SELECT p.title
                      FROM hr_service.positions p
                     WHERE p.position_code = e.position_code
                       AND p.is_deleted = FALSE) AS "positionName",
                   (SELECT p.base_salary_min
                      FROM hr_service.positions p
                     WHERE p.position_code = e.position_code
                       AND p.is_deleted = FALSE) AS "positionSalaryMin",
                   (SELECT p.base_salary_max
                      FROM hr_service.positions p
                     WHERE p.position_code = e.position_code
                       AND p.is_deleted = FALSE) AS "positionSalaryMax",
                   e.employment_type AS "employmentType",
                   e.hire_date AS "hireDate",
                   e.contract_end AS "contractEnd",
                   e.status,
                   e.is_deleted AS "isDeleted"
              FROM hr_service.employees e
             WHERE e.is_deleted = FALSE
            """;

    @Override
    public Page<EmployeeResponse> search(EmployeeSearchRequest request) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(request.getSearch())) {
            sql.append("""
                     AND (e.employee_code ILIKE :search
                          OR e.full_name ILIKE :search
                          OR e.email ILIKE :search
                          OR e.phone ILIKE :search
                          OR EXISTS (SELECT 1 FROM hr_service.departments d
                                      WHERE d.dept_code = e.department_code
                                        AND d.is_deleted = FALSE
                                        AND d.name ILIKE :search)
                          OR EXISTS (SELECT 1 FROM hr_service.positions p
                                      WHERE p.position_code = e.position_code
                                        AND p.is_deleted = FALSE
                                        AND p.title ILIKE :search))
                    """);
            params.put("search", appendLikeEscapee(request.getSearch().toLowerCase(Locale.ROOT)));
        }
        if (StringUtils.isNotBlank(request.getDepartmentCode())) {
            sql.append(" AND e.department_code = :departmentCode");
            params.put("departmentCode", request.getDepartmentCode());
        }
        if (StringUtils.isNotBlank(request.getPositionCode())) {
            sql.append(" AND e.position_code = :positionCode");
            params.put("positionCode", request.getPositionCode());
        }
        if (StringUtils.isNotBlank(request.getStatus())) {
            sql.append(" AND e.status = :status");
            params.put("status", request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getEmploymentType())) {
            sql.append(" AND e.employment_type = :employmentType");
            params.put("employmentType", request.getEmploymentType());
        }

        return getListPagination(
                sql.toString(),
                params,
                request,
                EmployeeResponse.class,
                "full_name ASC, id ASC"
        );
    }
}
