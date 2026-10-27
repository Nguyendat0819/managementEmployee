package com.example.notification_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull Long userId,
        @Size(max = 20) String channel,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content,
        @Size(max = 100) String category,
        @Pattern(regexp = "info|success|warning|default") @Size(max = 20) String type,
        @Size(max = 50) String icon,
        @Size(max = 50) String referenceType,
        Long referenceId
) {
}
