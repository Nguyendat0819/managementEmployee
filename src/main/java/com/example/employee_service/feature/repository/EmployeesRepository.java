package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.repository.custom.EmployeeRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeesRepository extends BaseRepository<Employees, Long>, EmployeeRepositoryCustom {

    List<Employees> findAllByIsDeletedFalseOrderByFullNameAsc();

    List<Employees> findAllByIsDeletedFalseAndDepartmentCodeOrderByFullNameAsc(String departmentCode);

    boolean existsByDepartmentCodeAndIsDeletedFalse(String departmentCode);

    boolean existsByPositionCodeAndIsDeletedFalse(String positionCode);

    long countByPositionCodeAndIsDeletedFalse(String positionCode);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    Optional<Employees> findByEmployeeCodeIgnoreCaseAndIsDeletedFalse(String employeeCode);

    Optional<Employees> findByUserIdAndIsDeletedFalse(Long userId);

    List<Employees> findAllByEmailIgnoreCaseAndIsDeletedFalse(String email);

    @Query("""
            SELECT e FROM Employees e
            WHERE e.isDeleted = false
              AND (:search = '' OR LOWER(e.employeeCode) LIKE CONCAT('%', :search, '%')
                   OR LOWER(e.fullName) LIKE CONCAT('%', :search, '%')
                   OR LOWER(e.email) LIKE CONCAT('%', :search, '%')
                   OR LOWER(e.phone) LIKE CONCAT('%', :search, '%')
                   OR e.departmentCode IN (SELECT d.deptCode FROM Departments d WHERE LOWER(d.name) LIKE CONCAT('%', :search, '%'))
                   OR e.positionCode IN (SELECT p.positionCode FROM Positions p WHERE LOWER(p.title) LIKE CONCAT('%', :search, '%')))
              AND (:departmentCode IS NULL OR e.departmentCode = :departmentCode)
              AND (:positionCode IS NULL OR e.positionCode = :positionCode)
              AND (:status IS NULL OR e.status = :status)
              AND (:employmentType IS NULL OR e.employmentType = :employmentType)
            ORDER BY e.fullName ASC
            """)
    List<Employees> search(
            @Param("search") String search,
            @Param("departmentCode") String departmentCode,
            @Param("positionCode") String positionCode,
            @Param("status") String status,
            @Param("employmentType") String employmentType);
}
