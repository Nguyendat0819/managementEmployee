package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "mã vai trò không hợp lệ")
    private String roleCode;
}
