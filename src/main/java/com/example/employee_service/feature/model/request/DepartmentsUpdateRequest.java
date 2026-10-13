package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentsUpdateRequest {
    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Level không được để trống")
    private Short level;
}
