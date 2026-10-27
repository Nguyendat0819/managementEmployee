package com.example.notification_service.feature.controller.api;

import com.example.notification_service.common.response.ApiResponse;
import com.example.notification_service.common.security.RequiresPermission;
import com.example.notification_service.feature.model.request.CreateNotificationRequest;
import com.example.notification_service.feature.model.response.NotificationReadResultResponse;
import com.example.notification_service.feature.model.response.NotificationResponse;
import com.example.notification_service.feature.model.response.UnreadNotificationCountResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/notifications")
public interface NotificationApi {

    @GetMapping("/me")
    @RequiresPermission("employee:notification:read")
    ApiResponse<List<NotificationResponse>> getMine();

    @GetMapping("/me/unread-count")
    @RequiresPermission("employee:notification:read")
    ApiResponse<UnreadNotificationCountResponse> getUnreadCount();

    @PatchMapping("/me/{id}/read")
    @RequiresPermission("employee:notification:read")
    ApiResponse<NotificationResponse> markAsRead(@PathVariable Long id);

    @PatchMapping("/me/read-all")
    @RequiresPermission("employee:notification:read")
    ApiResponse<NotificationReadResultResponse> markAllAsRead();

    @PostMapping
    @RequiresPermission("employee:notification:create")
    ApiResponse<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request);
}
