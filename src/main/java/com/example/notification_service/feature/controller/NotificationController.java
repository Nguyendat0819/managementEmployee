package com.example.notification_service.feature.controller;

import com.example.notification_service.common.response.ApiResponse;
import com.example.notification_service.common.response.ApiResponseFactory;
import com.example.notification_service.common.util.SecurityUtil;
import com.example.notification_service.feature.controller.api.NotificationApi;
import com.example.notification_service.feature.model.request.CreateNotificationRequest;
import com.example.notification_service.feature.model.response.NotificationReadResultResponse;
import com.example.notification_service.feature.model.response.NotificationResponse;
import com.example.notification_service.feature.model.response.UnreadNotificationCountResponse;
import com.example.notification_service.feature.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<NotificationResponse>> getMine() {
        return responseFactory.success(notificationService.getMine(SecurityUtil.getCurrentUserId()));
    }

    @Override
    public ApiResponse<UnreadNotificationCountResponse> getUnreadCount() {
        long unreadCount = notificationService.getUnreadCount(SecurityUtil.getCurrentUserId());
        return responseFactory.success(new UnreadNotificationCountResponse(unreadCount));
    }

    @Override
    public ApiResponse<NotificationResponse> markAsRead(Long id) {
        return responseFactory.success(notificationService.markAsRead(id, SecurityUtil.getCurrentUserId()));
    }

    @Override
    public ApiResponse<NotificationReadResultResponse> markAllAsRead() {
        int updatedCount = notificationService.markAllAsRead(SecurityUtil.getCurrentUserId());
        return responseFactory.success(new NotificationReadResultResponse(updatedCount));
    }

    @Override
    public ApiResponse<NotificationResponse> create(CreateNotificationRequest request) {
        return responseFactory.success(notificationService.create(request));
    }
}
