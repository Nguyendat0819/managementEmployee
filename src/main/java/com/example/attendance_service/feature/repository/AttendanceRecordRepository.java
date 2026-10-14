package com.example.attendance_service.feature.repository;

import com.example.attendance_service.feature.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByEmployeeCodeAndRecordedAt(String employeeCode, LocalDate recordedAt);

    List<AttendanceRecord> findByEmployeeCodeAndRecordedAtBetweenOrderByRecordedAtDesc(
            String employeeCode, LocalDate from, LocalDate to);
}
