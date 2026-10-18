package com.example.payroll_service.feature.task.entity;

import com.example.payroll_service.common.persistence.dto.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "employee_task_progress_submissions", schema = "hr_service")
public class EmployeeTaskProgress extends BaseEntity {
    @Column(name = "task_id", nullable = false)
    Long taskId;

    @Column(name = "proposed_percent")
    Integer proposedPercent;

    @Column(name = "result", nullable = false, length = 4000)
    String result;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_links", nullable = false, columnDefinition = "jsonb")
    List<String> evidenceLinks;

    @Column(name = "status", nullable = false, length = 20)
    String status;

    @Column(name = "submitted_at", nullable = false)
    LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;

    @Column(name = "reviewed_by", length = 255)
    String reviewedBy;

    @Column(name = "review_comment", length = 1000)
    String reviewComment;
}
