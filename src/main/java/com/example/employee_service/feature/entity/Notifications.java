package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "notifications", schema = "hr_service")
public class Notifications extends BaseEntity {

    @Column(name = "user", nullable = false)
    Long user;

    @Column(name = "channel", nullable = false, length = 20)
    String channel;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    String content;

    @Column(name = "sent_at")
    LocalDateTime sentAt;
}
