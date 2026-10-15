package com.example.attendance_service.feature.entity;

import com.example.attendance_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_records", schema = "hr_service",
        uniqueConstraints = @jakarta.persistence.UniqueConstraint(
                name = "uk_attendance_employee_date", columnNames = {"employee_code", "recorded_at"}))
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceRecord extends BaseEntity {

    @Column(name = "employee_code", nullable = false, length = 50)
    String employeeCode;

    @Column(name = "shift_id")
    Long shiftId;

    @Column(name = "shift_name", length = 100)
    String shiftName;

    @Column(name = "scheduled_start_time")
    LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time")
    LocalTime scheduledEndTime;

    @Column(name = "late_minutes")
    Integer lateMinutes;

    @Column(name = "early_minutes")
    Integer earlyMinutes;

    @Column(name = "check_in")
    LocalDateTime checkIn;

    @Column(name = "check_out")
    LocalDateTime checkOut;

    @Column(name = "work_duration_min")
    Integer workDurationMin;

    @Column(name = "overtime_min", nullable = false)
    Integer overtimeMin = 0;

    @Column(name = "status", nullable = false, length = 30)
    String status;

    @Column(name = "note", columnDefinition = "text")
    String note;

    @Column(name = "recorded_at", nullable = false)
    LocalDate recordedAt;
}
