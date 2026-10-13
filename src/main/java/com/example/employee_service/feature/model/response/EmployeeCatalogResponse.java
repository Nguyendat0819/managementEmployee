package com.example.employee_service.feature.model.response;

import java.util.List;

public record EmployeeCatalogResponse(
        List<String> genders,
        List<String> employmentTypes,
        List<String> statuses
) {
}
