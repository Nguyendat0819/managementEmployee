package com.example.notification_service.feature.entity;

import com.example.notification_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notifications", schema = "hr_service")
public class Notification extends BaseEntity {

    @Column(name = "\"user\"", nullable = false)
    Long user;

    @Column(name = "channel", nullable = false, length = 20)
    String channel;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    String content;

    @Column(name = "sent_at")
    LocalDateTime sentAt;

    @Column(name = "category", nullable = false, length = 100)
    String category = "Thông tin chung";

    @Column(name = "type", nullable = false, length = 20)
    String type = "info";

    @Column(name = "icon", nullable = false, length = 50)
    String icon = "bell";

    @Column(name = "is_read", nullable = false)
    Boolean read = false;

    @Column(name = "read_at")
    LocalDateTime readAt;

    @Column(name = "reference_type", length = 50)
    String referenceType;

    @Column(name = "reference_id")
    Long referenceId;
}
