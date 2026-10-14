package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.EmployeeContracts;
import com.example.employee_service.feature.repository.custom.EmployeeContractRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeContractsRepository extends BaseRepository<EmployeeContracts, Long>, EmployeeContractRepositoryCustom {

    boolean existsByContractNoIgnoreCase(String contractNo);

    boolean existsByContractNoIgnoreCaseAndIdNot(String contractNo, Long id);

    Optional<EmployeeContracts> findByIdAndIsDeletedFalse(Long id);
}
