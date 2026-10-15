package com.example.attendance_service.feature.entity;

import com.example.attendance_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.time.LocalTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "shifts", schema = "hr_service")
public class Shift extends BaseEntity {

    @Column(name = "shift_name", nullable = false, length = 100)
    String shiftName;

    @Column(name = "start_time", nullable = false)
    LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalTime endTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "day_of_week", nullable = false, columnDefinition = "jsonb")
    List<String> dayOfWeek;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted = false;
}
