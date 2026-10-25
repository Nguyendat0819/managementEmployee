package com.example.employee_service.feature.model.response;

import java.time.LocalDateTime;

public record PasswordResetRequestResponse(
        Long id,
        Long userId,
        String userName,
        String fullName,
        String email,
        LocalDateTime requestedAt
) {
}
