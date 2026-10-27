package com.example.notification_service.feature.model.response;

import com.example.notification_service.feature.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String content,
        String category,
        LocalDateTime createdAt,
        String icon,
        String type,
        boolean read,
        String referenceType,
        Long referenceId
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getCategory(),
                notification.getSentAt(),
                notification.getIcon(),
                notification.getType(),
                Boolean.TRUE.equals(notification.getRead()),
                notification.getReferenceType(),
                notification.getReferenceId()
        );
    }
}
