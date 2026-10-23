package com.example.leave_service.feature.repository;

import com.example.leave_service.common.persistence.BaseRepository;
import com.example.leave_service.feature.entity.LeaveType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeaveTypeRepository extends BaseRepository<LeaveType, Long> {

    List<LeaveType> findByIsDeletedFalseOrderByNameAsc();

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String code, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select leaveType from LeaveType leaveType where leaveType.id = :id and leaveType.isDeleted = false")
    Optional<LeaveType> findActiveByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select leaveType from LeaveType leaveType where leaveType.id = :id")
    Optional<LeaveType> findByIdForUpdate(@Param("id") Long id);
}
