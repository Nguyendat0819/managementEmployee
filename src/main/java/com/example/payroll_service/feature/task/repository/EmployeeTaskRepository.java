package com.example.payroll_service.feature.task.repository;

import com.example.payroll_service.common.persistence.BaseRepository;
import com.example.payroll_service.feature.task.entity.EmployeeTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeTaskRepository extends BaseRepository<EmployeeTask, Long> {
    List<EmployeeTask> findAllByOrderByCreatedAtDesc();

    List<EmployeeTask> findAllByEmployeeCodeIgnoreCaseOrderByDueDateAscCreatedAtDesc(String employeeCode);

    List<EmployeeTask> findAllByPeriodMonthOrderByEmployeeCodeAscCreatedAtDesc(LocalDate periodMonth);

    Optional<EmployeeTask> findByIdAndEmployeeCodeIgnoreCase(Long id, String employeeCode);

    @Query("select lower(t.employeeCode) as employeeCode, sum(t.bonusAmount) as bonusAmount, count(t.id) as taskCount " +
            "from EmployeeTask t where t.status = 'APPROVED' " +
            "and t.periodMonth >= :from and t.periodMonth < :to " +
            "group by lower(t.employeeCode)")
    List<TaskBonusSummary> summarizeApprovedBonuses(@Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);

    @Query("select lower(t.employeeCode) as employeeCode, sum(t.penaltyAmount) as penaltyAmount, count(t.id) as taskCount " +
            "from EmployeeTask t where t.status = 'REJECTED' " +
            "and t.periodMonth >= :from and t.periodMonth < :to " +
            "group by lower(t.employeeCode)")
    List<TaskPenaltySummary> summarizeReviewedTaskPenalties(@Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);

    interface TaskBonusSummary {
        String getEmployeeCode();
        BigDecimal getBonusAmount();
        Long getTaskCount();
    }

    interface TaskPenaltySummary {
        String getEmployeeCode();
        BigDecimal getPenaltyAmount();
        Long getTaskCount();
    }
}
