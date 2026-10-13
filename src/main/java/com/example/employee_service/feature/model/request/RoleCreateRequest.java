package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới")
    private String roleCode;

    @NotBlank
    @Size(max = 255)
    private String roleName;

    @Size(max = 255)
    private String description;
}
