package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String roleName;

    @Size(max = 255)
    private String description;
}
