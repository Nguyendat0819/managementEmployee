package com.example.notification_service.feature.repository;

import com.example.notification_service.feature.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop100ByUserOrderBySentAtDesc(Long user);

    long countByUserAndReadFalse(Long user);

    Optional<Notification> findByIdAndUser(Long id, Long user);

    @Modifying
    @Query("update Notification notification set notification.read = true, notification.readAt = :readAt " +
            "where notification.user = :userId and notification.read = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
