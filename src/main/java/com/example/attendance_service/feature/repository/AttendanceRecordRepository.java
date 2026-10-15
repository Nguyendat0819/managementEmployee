package com.example.attendance_service.feature.repository;

import com.example.attendance_service.feature.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByEmployeeCodeAndRecordedAt(String employeeCode, LocalDate recordedAt);

    Optional<AttendanceRecord> findFirstByEmployeeCodeAndCheckOutIsNullOrderByRecordedAtDesc(String employeeCode);

    List<AttendanceRecord> findByEmployeeCodeAndRecordedAtBetweenOrderByRecordedAtDesc(
            String employeeCode, LocalDate from, LocalDate to);

    @Query("""
            select a from AttendanceRecord a
            where a.recordedAt between :from and :to
              and (:employeeCode is null or lower(a.employeeCode) like lower(concat('%', :employeeCode, '%')))
              and (:status is null or a.status = :status)
            order by a.recordedAt desc, a.checkIn desc
            """)
    Page<AttendanceRecord> search(@Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  @Param("employeeCode") String employeeCode,
                                  @Param("status") String status,
                                  Pageable pageable);
}
