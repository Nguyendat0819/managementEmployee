package com.example.payroll_service.feature.task.repository;

import com.example.payroll_service.common.persistence.BaseRepository;
import com.example.payroll_service.feature.task.entity.EmployeeTaskProgress;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeTaskProgressRepository extends BaseRepository<EmployeeTaskProgress, Long> {
    List<EmployeeTaskProgress> findAllByTaskIdInOrderBySubmittedAtDesc(Collection<Long> taskIds);

    List<EmployeeTaskProgress> findAllByTaskIdOrderBySubmittedAtDesc(Long taskId);

    Optional<EmployeeTaskProgress> findFirstByTaskIdOrderBySubmittedAtDesc(Long taskId);
}
