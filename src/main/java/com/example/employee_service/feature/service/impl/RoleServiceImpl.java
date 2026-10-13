package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.RolePermissions;
import com.example.employee_service.feature.entity.Roles;
import com.example.employee_service.feature.model.request.RoleCreateRequest;
import com.example.employee_service.feature.model.request.RoleSearchRequest;
import com.example.employee_service.feature.model.request.RoleUpdateRequest;
import com.example.employee_service.feature.model.response.PositionResponse;
import com.example.employee_service.feature.model.response.RoleResponse;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.repository.RolePermissionRepository;
import com.example.employee_service.feature.repository.RolesRepository;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    private static final Set<String> PROTECTED_ROLE_CODES = Set.of("ADMIN", "EMPLOYEE");

    PositionsRepository positionsRepository;
    RolesRepository rolesRepository;
    UsersRepository usersRepository;
    RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> searchRoles(RoleSearchRequest request) {
        request.setSearch(normalize(request.getSearch()));
        request.setRoleCode(normalizeCode(request.getRoleCode()));
        return PageResponse.of(rolesRepository.search(request));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(Long id) {
        return toRoleResponse(findRole(id));
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        String roleCode = normalizeCode(request.getRoleCode());
        if (rolesRepository.existsByRoleCodeIgnoreCase(roleCode)) {
            throw new BusinessException(DomainCode.CONFLICT, "mã vai trò " + roleCode);
        }

        Roles role = new Roles();
        role.setRoleCode(roleCode);
        role.setRoleName(request.getRoleName().trim());
        role.setDescription(normalize(request.getDescription()));
        role.setIsDeleted(false);
        return toRoleResponse(rolesRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        Roles role = activeRole(id);
        role.setRoleName(request.getRoleName().trim());
        role.setDescription(normalize(request.getDescription()));
        return toRoleResponse(rolesRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Roles role = activeRole(id);
        if (PROTECTED_ROLE_CODES.contains(role.getRoleCode().toUpperCase(Locale.ROOT))) {
            throw new BusinessException(DomainCode.CONFLICT, "vai trò hệ thống " + role.getRoleCode());
        }
        if (usersRepository.countByRoleCodeAndIsDeletedFalse(role.getRoleCode()) > 0) {
            throw new BusinessException(DomainCode.CONFLICT, "vai trò đang được gán cho người dùng");
        }

        role.setIsDeleted(true);
        rolesRepository.save(role);
        List<RolePermissions> permissions = rolePermissionRepository
                .findAllByRoleCodeAndIsDeletedFalse(role.getRoleCode());
        permissions.forEach(permission -> permission.setIsDeleted(true));
        rolePermissionRepository.saveAll(permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getPositions() {
        return positionsRepository.findAllByIsDeletedFalseOrderByTitleAsc().stream()
                .map(position -> new PositionResponse(position.getPositionCode(), position.getTitle()))
                .toList();
    }

    private Roles findRole(Long id) {
        return rolesRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "vai trò " + id));
    }

    private Roles activeRole(Long id) {
        Roles role = findRole(id);
        if (Boolean.TRUE.equals(role.getIsDeleted())) {
            throw new BusinessException(DomainCode.NOT_FOUND, "vai trò " + id);
        }
        return role;
    }

    private RoleResponse toRoleResponse(Roles role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .userCount(usersRepository.countByRoleCodeAndIsDeletedFalse(role.getRoleCode()))
                .permissionCount(rolePermissionRepository.countByRoleCodeAndIsDeletedFalse(role.getRoleCode()))
                .isDeleted(role.getIsDeleted())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeCode(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }
}
