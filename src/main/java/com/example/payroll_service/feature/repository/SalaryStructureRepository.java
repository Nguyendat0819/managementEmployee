package com.example.payroll_service.feature.repository;

import com.example.payroll_service.common.persistence.BaseRepository;
import com.example.payroll_service.feature.entity.SalaryStructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends BaseRepository<SalaryStructure, Long> {
    List<SalaryStructure> findAllByOrderByEffectiveDateDescPositionCodeAsc();

    Optional<SalaryStructure> findByPositionCodeIgnoreCaseAndEffectiveDate(String positionCode, LocalDate effectiveDate);
}
