package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Departments;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentsRepository extends BaseRepository<Departments, Long> {
    Departments findByDeptCodeAndIsDeleted(String deptCode, Boolean isDeleted);

    @Query("""
            SELECT d FROM Departments d
            WHERE d.isDeleted = :isDeleted
              AND (:search = ''
                   OR LOWER(d.deptCode) LIKE CONCAT('%', :search, '%')
                   OR LOWER(d.name) LIKE CONCAT('%', :search, '%')
                   OR LOWER(d.code) LIKE CONCAT('%', :search, '%'))
              AND (:level IS NULL OR d.level = :level)
              AND (:parentCode IS NULL OR :parentCode = '' OR d.parentCode = :parentCode)
            ORDER BY d.level ASC, d.name ASC
            """)
    List<Departments> findByFilters(
            @Param("search") String search,
            @Param("level") Short level,
            @Param("parentCode") String parentCode,
            @Param("isDeleted") Boolean isDeleted
    );

    boolean existsByParentCodeAndIsDeletedFalse(String parentCode);

    List<Departments> findAllByIsDeletedFalseOrderByLevelAscNameAsc();
}
