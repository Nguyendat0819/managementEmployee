package com.example.employee_service.feature.model.response;

public record MenuResponse(
        Long id,
        String menuCode,
        String label,
        String icon,
        String iconColor,
        String path,
        String parentCode,
        int sortOrder
) {
}
