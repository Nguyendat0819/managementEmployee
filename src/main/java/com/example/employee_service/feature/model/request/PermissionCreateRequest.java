package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionCreateRequest {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_.:-]+$", message = "chỉ được chứa chữ, số, dấu chấm, gạch ngang, gạch dưới và dấu hai chấm")
    private String permissionCode;

    @NotBlank
    @Size(max = 50)
    private String permissionName;

    @Size(max = 255)
    private String description;
}
