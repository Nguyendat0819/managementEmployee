package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.EmployeeContracts;
import com.example.employee_service.feature.repository.custom.EmployeeContractRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeContractsRepository extends BaseRepository<EmployeeContracts, Long>, EmployeeContractRepositoryCustom {

    boolean existsByContractNoIgnoreCase(String contractNo);

    boolean existsByContractNoIgnoreCaseAndIdNot(String contractNo, Long id);

    Optional<EmployeeContracts> findByIdAndIsDeletedFalse(Long id);

    List<EmployeeContracts> findAllByEmployeeCodeIgnoreCaseAndIsDeletedFalseOrderByStartDateDescIdDesc(String employeeCode);

    @Query("""
            select c from EmployeeContracts c, Employees e
            where upper(c.employeeCode) = upper(e.employeeCode)
              and e.isDeleted = false
              and e.status in ('Đang làm việc', 'Đang thử việc', 'Tạm nghỉ')
              and c.isDeleted = false
              and c.status in ('Đang hiệu lực', 'Sắp hết hạn')
              and c.startDate <= :to
              and (c.endDate is null or c.endDate >= :from)
            order by c.employeeCode asc, c.startDate asc, c.id asc
            """)
    List<EmployeeContracts> findPayrollContracts(@Param("from") java.time.LocalDate from,
                                                @Param("to") java.time.LocalDate to);
}
