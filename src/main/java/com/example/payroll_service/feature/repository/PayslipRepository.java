package com.example.payroll_service.feature.repository;

import com.example.payroll_service.common.persistence.BaseRepository;
import com.example.payroll_service.feature.entity.Payslip;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends BaseRepository<Payslip, Long> {
    List<Payslip> findByRunOrderByEmployeeCodeAsc(Long run);

    List<Payslip> findByEmployeeCodeIgnoreCaseOrderByRunDesc(String employeeCode);

    boolean existsByRunAndEmployeeCodeIgnoreCase(Long run, String employeeCode);

    Optional<Payslip> findByIdAndEmployeeCodeIgnoreCase(Long id, String employeeCode);

    void deleteByRun(Long run);
}
