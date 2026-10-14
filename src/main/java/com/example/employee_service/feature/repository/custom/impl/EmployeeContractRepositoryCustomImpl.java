package com.example.employee_service.feature.repository.custom.impl;

import com.example.employee_service.common.persistence.NativeQuerySupport;
import com.example.employee_service.feature.model.request.EmployeeContractSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeContractResponse;
import com.example.employee_service.feature.repository.custom.EmployeeContractRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class EmployeeContractRepositoryCustomImpl extends NativeQuerySupport
        implements EmployeeContractRepositoryCustom {

    private static final String SELECT_SQL = """
            SELECT c.id,
                   c.contract_no AS "contractNo",
                   c.contract_type AS "contractType",
                   c.employee_code AS "employeeCode",
                   c.full_name AS "fullName",
                   c.birth_date AS "birthDate",
                   c.id_card AS "idCard",
                   c.department_code AS "departmentCode",
                   c.department_name AS "departmentName",
                   c.position_code AS "positionCode",
                   c.position_name AS "positionName",
                   c.actual_salary AS "actualSalary",
                   c.employment_type AS "employmentType",
                   c.hire_date AS "hireDate",
                   c.start_date AS "startDate",
                   c.end_date AS "endDate",
                   c.signed_date AS "signedDate",
                   c.status,
                   c.is_deleted AS "isDeleted",
                   c.created_at AS "createdAt",
                   c.updated_at AS "updatedAt"
              FROM hr_service.employee_contracts c
             WHERE 1 = 1
            """;

    @Override
    public Page<EmployeeContractResponse> search(EmployeeContractSearchRequest request) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(request.getSearch())) {
            sql.append("""
                     AND (c.contract_no ILIKE :search
                          OR c.employee_code ILIKE :search
                          OR c.full_name ILIKE :search
                          OR c.department_name ILIKE :search
                          OR c.position_name ILIKE :search)
                    """);
            params.put("search", appendLikeEscapee(request.getSearch()));
        }
        if (StringUtils.isNotBlank(request.getContractType())) {
            sql.append(" AND c.contract_type = :contractType");
            params.put("contractType", request.getContractType());
        }
        if (StringUtils.isNotBlank(request.getEmployeeCode())) {
            sql.append(" AND c.employee_code = :employeeCode");
            params.put("employeeCode", request.getEmployeeCode());
        }
        if (StringUtils.isNotBlank(request.getEmploymentType())) {
            sql.append(" AND c.employment_type = :employmentType");
            params.put("employmentType", request.getEmploymentType());
        }
        if (StringUtils.isNotBlank(request.getStatus())) {
            sql.append(" AND c.status = :status");
            params.put("status", request.getStatus());
        }
        if (request.getIsDeleted() != null) {
            sql.append(" AND c.is_deleted = :isDeleted");
            params.put("isDeleted", request.getIsDeleted());
        }

        return getListPagination(
                sql.toString(),
                params,
                request,
                EmployeeContractResponse.class,
                "start_date DESC, id DESC"
        );
    }
}
