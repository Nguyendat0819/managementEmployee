package com.example.notification_service.feature.service;

import com.example.notification_service.feature.model.request.CreateNotificationRequest;
import com.example.notification_service.feature.model.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMine(Long userId);
    long getUnreadCount(Long userId);
    NotificationResponse create(CreateNotificationRequest request);
    NotificationResponse markAsRead(Long id, Long userId);
    int markAllAsRead(Long userId);
}
