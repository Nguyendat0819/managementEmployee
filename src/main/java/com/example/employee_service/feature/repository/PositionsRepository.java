package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Positions;
import com.example.employee_service.feature.repository.custom.PositionRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionsRepository extends BaseRepository<Positions, Long>, PositionRepositoryCustom {
    List<Positions> findAllByIsDeletedFalseOrderByTitleAsc();
    boolean existsByPositionCodeAndIsDeletedFalse(String positionCode);
    boolean existsByPositionCodeIgnoreCase(String positionCode);
    Optional<Positions> findByPositionCodeAndIsDeletedFalse(String positionCode);
}
