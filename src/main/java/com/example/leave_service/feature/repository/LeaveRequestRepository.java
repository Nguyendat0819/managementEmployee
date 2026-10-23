package com.example.leave_service.feature.repository;

import com.example.leave_service.common.persistence.BaseRepository;
import com.example.leave_service.feature.entity.LeaveRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends BaseRepository<LeaveRequest, Long>,
        JpaSpecificationExecutor<LeaveRequest> {

    List<LeaveRequest> findByRequesterUserIdOrderByCreatedAtDesc(Long requesterUserId);

    Optional<LeaveRequest> findByIdAndRequesterUserId(Long id, Long requesterUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select leaveRequest from LeaveRequest leaveRequest where leaveRequest.id = :id")
    Optional<LeaveRequest> findByIdForUpdate(@Param("id") Long id);

    List<LeaveRequest> findByRequesterUserIdAndTypeAndStatusInAndStartDateBetween(
            Long requesterUserId, Long type, Collection<String> statuses, LocalDate from, LocalDate to);

    List<LeaveRequest> findByEmployeeCodeAndTypeAndStatusInAndStartDateBetween(
            String employeeCode, Long type, Collection<String> statuses, LocalDate from, LocalDate to);

    List<LeaveRequest> findByRequesterUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
            Long requesterUserId, LocalDate endDate, LocalDate startDate, Collection<String> statuses);
}
