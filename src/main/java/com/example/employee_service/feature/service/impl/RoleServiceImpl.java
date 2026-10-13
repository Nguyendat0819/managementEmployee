package com.example.employee_service.feature.service.impl;

import com.example.employee_service.feature.model.response.PositionResponse;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    PositionsRepository positionsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getPositions() {
        return positionsRepository.findAllByIsDeletedFalseOrderByTitleAsc().stream()
                .map(position -> new PositionResponse(position.getPositionCode(), position.getTitle()))
                .toList();
    }
}
