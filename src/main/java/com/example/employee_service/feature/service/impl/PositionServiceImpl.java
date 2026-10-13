package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.Positions;
import com.example.employee_service.feature.model.request.PositionCreateRequest;
import com.example.employee_service.feature.model.request.PositionSearchRequest;
import com.example.employee_service.feature.model.request.PositionUpdateRequest;
import com.example.employee_service.feature.model.response.PositionManagementResponse;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.service.PositionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PositionServiceImpl implements PositionService {
    PositionsRepository positionsRepository;
    EmployeesRepository employeesRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PositionManagementResponse> searchPositions(PositionSearchRequest request) {
        request.setSearch(normalize(request.getSearch()));
        request.setPositionCode(normalizeCode(request.getPositionCode()));
        request.setLevel(normalize(request.getLevel()));
        return PageResponse.of(positionsRepository.search(request));
    }

    @Override
    @Transactional(readOnly = true)
    public PositionManagementResponse getPosition(Long id) {
        return toResponse(findPosition(id));
    }

    @Override
    @Transactional
    public PositionManagementResponse createPosition(PositionCreateRequest request) {
        String code = normalizeCode(request.getPositionCode());
        if (positionsRepository.existsByPositionCodeIgnoreCase(code)) {
            throw new BusinessException(DomainCode.CONFLICT, "mã chức vụ " + code);
        }
        validateSalaryRange(request.getBaseSalaryMin(), request.getBaseSalaryMax());

        Positions position = new Positions();
        position.setPositionCode(code);
        position.setTitle(request.getTitle().trim());
        position.setLevel(request.getLevel().trim());
        position.setBaseSalaryMin(request.getBaseSalaryMin());
        position.setBaseSalaryMax(request.getBaseSalaryMax());
        position.setIsDeleted(false);
        return toResponse(positionsRepository.save(position));
    }

    @Override
    @Transactional
    public PositionManagementResponse updatePosition(Long id, PositionUpdateRequest request) {
        Positions position = activePosition(id);
        validateSalaryRange(request.getBaseSalaryMin(), request.getBaseSalaryMax());
        position.setTitle(request.getTitle().trim());
        position.setLevel(request.getLevel().trim());
        position.setBaseSalaryMin(request.getBaseSalaryMin());
        position.setBaseSalaryMax(request.getBaseSalaryMax());
        return toResponse(positionsRepository.save(position));
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        Positions position = activePosition(id);
        if (employeesRepository.existsByPositionCodeAndIsDeletedFalse(position.getPositionCode())) {
            throw new BusinessException(DomainCode.CONFLICT, "chức vụ đang được gán cho nhân viên");
        }
        position.setIsDeleted(true);
        positionsRepository.save(position);
    }

    private Positions findPosition(Long id) {
        return positionsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "chức vụ " + id));
    }

    private Positions activePosition(Long id) {
        Positions position = findPosition(id);
        if (Boolean.TRUE.equals(position.getIsDeleted())) {
            throw new BusinessException(DomainCode.NOT_FOUND, "chức vụ " + id);
        }
        return position;
    }

    private PositionManagementResponse toResponse(Positions position) {
        return PositionManagementResponse.builder()
                .id(position.getId())
                .positionCode(position.getPositionCode())
                .title(position.getTitle())
                .level(position.getLevel())
                .baseSalaryMin(position.getBaseSalaryMin())
                .baseSalaryMax(position.getBaseSalaryMax())
                .employeeCount(employeesRepository.countByPositionCodeAndIsDeletedFalse(position.getPositionCode()))
                .isDeleted(position.getIsDeleted())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    private void validateSalaryRange(java.math.BigDecimal min, java.math.BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "lương tối thiểu không được lớn hơn lương tối đa");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeCode(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }
}
