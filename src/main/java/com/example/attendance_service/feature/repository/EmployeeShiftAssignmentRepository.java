package com.example.attendance_service.feature.repository;

import com.example.attendance_service.feature.entity.EmployeeShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Collection;

public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {

    List<EmployeeShiftAssignment> findAllByOrderByEffectiveDateDesc();

    long countByShift(Long shift);

    @Query("""
            select case when count(assignment) > 0 then true else false end
            from EmployeeShiftAssignment assignment
            where assignment.employeeCode = :employeeCode
              and (:assignmentId is null or assignment.id <> :assignmentId)
              and (:endDate is null or assignment.effectiveDate <= :endDate)
              and (assignment.endDate is null or assignment.endDate >= :effectiveDate)
            """)
    boolean existsOverlappingAssignmentForEmployee(@Param("employeeCode") String employeeCode,
                                                   @Param("assignmentId") Long assignmentId,
                                                   @Param("effectiveDate") LocalDate effectiveDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("""
            select assignment from EmployeeShiftAssignment assignment
            where assignment.employeeCode = :employeeCode
              and assignment.effectiveDate <= :workDate
              and (assignment.endDate is null or assignment.endDate >= :workDate)
            order by assignment.effectiveDate desc
            """)
    List<EmployeeShiftAssignment> findActiveAssignments(@Param("employeeCode") String employeeCode,
                                                         @Param("workDate") LocalDate workDate);

    @Query("""
            select assignment from EmployeeShiftAssignment assignment
            where lower(assignment.employeeCode) in :employeeCodes
              and assignment.effectiveDate <= :to
              and (assignment.endDate is null or assignment.endDate >= :from)
            order by assignment.effectiveDate asc
            """)
    List<EmployeeShiftAssignment> findPayrollAssignments(@Param("employeeCodes") Collection<String> employeeCodes,
                                                          @Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);
}
