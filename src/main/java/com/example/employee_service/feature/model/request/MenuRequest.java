package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MenuRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = "^[a-z0-9][a-z0-9-]*$") String menuCode,
        @NotBlank @Size(max = 255) String label,
        @NotBlank @Size(max = 100) String icon,
        @Size(max = 20) String iconColor,
        @Size(max = 255) @Pattern(regexp = "^/[A-Za-z0-9/_-]+$") String path,
        @Size(max = 100) String parentCode,
        int sortOrder
) {
}
