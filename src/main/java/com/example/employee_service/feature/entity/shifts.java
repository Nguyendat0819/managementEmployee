package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "shifts", schema = "hr_service")
public class shifts extends BaseEntity {

    @Column(name = "shift_name", nullable = false, length = 100)
    String shiftName;

    @Column(name = "start_time", nullable = false)
    LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalTime endTime;

    @Column(name = "day_of_week", nullable = false, columnDefinition = "jsonb")
    String dayOfWeek;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted;
}
