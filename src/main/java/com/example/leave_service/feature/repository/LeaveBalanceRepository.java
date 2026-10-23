package com.example.leave_service.feature.repository;

import com.example.leave_service.feature.entity.LeaveBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @Modifying
    @Query(value = "INSERT INTO hr_service.leave_balances " +
            "(employee_code, requester_user_id, type, balance_year, total_days, used_days) " +
            "VALUES (:employeeCode, :userId, :typeId, :year, :totalDays, 0) " +
            "ON CONFLICT (requester_user_id, type, balance_year) DO NOTHING", nativeQuery = true)
    int createIfMissing(@Param("employeeCode") String employeeCode,
                        @Param("userId") Long userId,
                        @Param("typeId") Long typeId,
                        @Param("year") Integer year,
                        @Param("totalDays") java.math.BigDecimal totalDays);

    @Modifying
    @Query(value = "INSERT INTO hr_service.leave_balances " +
            "(employee_code, requester_user_id, type, balance_year, total_days, used_days) " +
            "VALUES (:employeeCode, NULL, :typeId, :year, :totalDays, 0) " +
            "ON CONFLICT (employee_code, type, balance_year) WHERE requester_user_id IS NULL DO NOTHING", nativeQuery = true)
    int createLegacyIfMissing(@Param("employeeCode") String employeeCode,
                              @Param("typeId") Long typeId,
                              @Param("year") Integer year,
                              @Param("totalDays") java.math.BigDecimal totalDays);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select balance from LeaveBalance balance where balance.requesterUserId = :userId " +
            "and balance.type = :typeId and balance.balanceYear = :year")
    Optional<LeaveBalance> findForUpdate(@Param("userId") Long userId,
                                        @Param("typeId") Long typeId,
                                        @Param("year") Integer year);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select balance from LeaveBalance balance where balance.requesterUserId is null " +
            "and balance.employeeCode = :employeeCode and balance.type = :typeId and balance.balanceYear = :year")
    Optional<LeaveBalance> findLegacyForUpdate(@Param("employeeCode") String employeeCode,
                                               @Param("typeId") Long typeId,
                                               @Param("year") Integer year);

    Optional<LeaveBalance> findByRequesterUserIdAndTypeAndBalanceYear(Long requesterUserId,
                                                                      Long type,
                                                                      Integer balanceYear);

    Optional<LeaveBalance> findByRequesterUserIdIsNullAndEmployeeCodeAndTypeAndBalanceYear(String employeeCode,
                                                                                             Long type,
                                                                                             Integer balanceYear);

    List<LeaveBalance> findByRequesterUserIdAndBalanceYearOrderByTypeAsc(Long requesterUserId, Integer balanceYear);
}
