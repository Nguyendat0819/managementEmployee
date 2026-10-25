package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.PasswordResetRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetRequestRepository extends BaseRepository<PasswordResetRequest, Long> {
    List<PasswordResetRequest> findAllByStatusOrderByRequestedAtAsc(String status);

    List<PasswordResetRequest> findAllByUserIdAndStatus(Long userId, String status);

    Optional<PasswordResetRequest> findFirstByUserIdAndStatus(Long userId, String status);
}
