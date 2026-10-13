package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.Permissions;
import com.example.employee_service.feature.entity.RolePermissions;
import com.example.employee_service.feature.model.request.PermissionCreateRequest;
import com.example.employee_service.feature.model.request.PermissionSearchRequest;
import com.example.employee_service.feature.model.request.PermissionUpdateRequest;
import com.example.employee_service.feature.model.response.PermissionResponse;
import com.example.employee_service.feature.repository.PermissionsRepository;
import com.example.employee_service.feature.repository.RolePermissionRepository;
import com.example.employee_service.feature.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionsRepository permissionsRepository;
    RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> search(PermissionSearchRequest request) {
        request.setSearch(normalize(request.getSearch()));
        return PageResponse.of(permissionsRepository.search(request));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermission(Long id) {
        return toResponse(findPermission(id));
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        String code = normalizeCode(request.getPermissionCode());
        if (permissionsRepository.existsByPermissionCodeIgnoreCase(code)) {
            throw new BusinessException(DomainCode.CONFLICT, "mã quyền " + code);
        }

        Permissions permission = new Permissions();
        permission.setPermissionCode(code);
        permission.setPermissionName(request.getPermissionName().trim());
        permission.setDescription(normalize(request.getDescription()));
        permission.setIsDeleted(false);
        return toResponse(permissionsRepository.save(permission));
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionUpdateRequest request) {
        Permissions permission = activePermission(id);
        permission.setPermissionName(request.getPermissionName().trim());
        permission.setDescription(normalize(request.getDescription()));
        return toResponse(permissionsRepository.save(permission));
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permissions permission = activePermission(id);
        permission.setIsDeleted(true);
        permissionsRepository.save(permission);

        List<RolePermissions> assignments = rolePermissionRepository
                .findAllByPermissionCodeAndIsDeletedFalse(permission.getPermissionCode());
        assignments.forEach(assignment -> assignment.setIsDeleted(true));
        rolePermissionRepository.saveAll(assignments);
    }

    private Permissions findPermission(Long id) {
        return permissionsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "quyền " + id));
    }

    private Permissions activePermission(Long id) {
        Permissions permission = findPermission(id);
        if (Boolean.TRUE.equals(permission.getIsDeleted())) {
            throw new BusinessException(DomainCode.NOT_FOUND, "quyền " + id);
        }
        return permission;
    }

    private PermissionResponse toResponse(Permissions permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .description(permission.getDescription())
                .isDeleted(permission.getIsDeleted())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeCode(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
