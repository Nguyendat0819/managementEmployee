package com.example.attendance_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
        @NotBlank(message = "employeeCode không được để trống") String employeeCode,
        String note
) {
}
