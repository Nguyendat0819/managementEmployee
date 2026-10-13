package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionUpdateRequest {
    @NotBlank
    @Size(max = 50)
    private String permissionName;

    @Size(max = 255)
    private String description;
}
