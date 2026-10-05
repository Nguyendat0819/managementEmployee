package com.example.employee_service.feature.entity;

import com.example.employee_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "attendance_records", schema = "hr_service")
public class AttendanceRecords extends BaseEntity {


    @Column(name = "employee_code", nullable = false, length = 255)
    String employeeCode;

    @Column(name = "check_in")
    LocalDateTime checkIn;

    @Column(name = "check_out")
    LocalDateTime checkOut;

    @Column(name = "work_duration_min")
    Integer workDurationMin;

    @Column(name = "overtime_min", nullable = false)
    Integer overtimeMin;

    @Column(name = "status", nullable = false, length = 50)
    String status;

    @Column(name = "note", columnDefinition = "text")
    String note;

    @Column(name = "recorded_at", nullable = false)
    LocalDate recordedAt;
}
