package com.example.notification_service.feature.service.impl;

import com.example.notification_service.common.exception.ResourceNotFoundException;
import com.example.notification_service.feature.entity.Notification;
import com.example.notification_service.feature.model.request.CreateNotificationRequest;
import com.example.notification_service.feature.model.response.NotificationResponse;
import com.example.notification_service.feature.repository.NotificationRepository;
import com.example.notification_service.feature.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMine(Long userId) {
        return notificationRepository.findTop100ByUserOrderBySentAtDesc(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserAndReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUser(request.userId());
        notification.setChannel(defaultIfBlank(request.channel(), "IN_APP"));
        notification.setTitle(request.title().trim());
        notification.setContent(request.content().trim());
        notification.setCategory(defaultIfBlank(request.category(), "Thông tin chung"));
        notification.setType(defaultIfBlank(request.type(), "info"));
        notification.setIcon(defaultIfBlank(request.icon(), "bell"));
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());
        notification.setReferenceType(blankToNull(request.referenceType()));
        notification.setReferenceId(request.referenceId());
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUser(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", id));
        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
