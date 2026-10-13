package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentsRequest {

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Size(max = 255)
    private String deptCode;

    @Size(max = 255)
    private String parentCode;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Code không được để trống")
    @Size(max = 20)
    private String code;

    @Size(max = 255)
    private String headCode;

    @NotNull(message = "Level không được để trống")
    private Short level;
}
