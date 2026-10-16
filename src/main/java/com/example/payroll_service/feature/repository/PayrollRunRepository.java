package com.example.payroll_service.feature.repository;

import com.example.payroll_service.common.persistence.BaseRepository;
import com.example.payroll_service.feature.entity.PayrollRun;

import java.util.List;

public interface PayrollRunRepository extends BaseRepository<PayrollRun, Long> {
    boolean existsByPeriodYearAndPeriodMonth(Short periodYear, Integer periodMonth);

    List<PayrollRun> findAllByOrderByPeriodYearDescPeriodMonthDesc();
}
