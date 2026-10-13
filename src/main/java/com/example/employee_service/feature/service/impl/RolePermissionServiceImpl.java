package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.util.Const;
import com.example.employee_service.feature.entity.Permissions;
import com.example.employee_service.feature.entity.RolePermissions;
import com.example.employee_service.feature.model.request.UpdateRolePermissionsRequest;
import com.example.employee_service.feature.repository.RolePermissionRepository;
import com.example.employee_service.feature.repository.PermissionsRepository;
import com.example.employee_service.feature.repository.RolesRepository;
import com.example.employee_service.feature.service.RolePermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.employee_service.common.util.SecurityUtil.getRoles;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionServiceImpl implements RolePermissionService {
    RolePermissionRepository rolePermissionRepository;
    PermissionsRepository permissionsRepository;
    RolesRepository rolesRepository;
    @Override
    public List<RolePermissionDto> getPermissionsByRoleCodes(List<String> roleCodes) {
        List<String> normalizedRoleCodes = roleCodes == null
                ? List.of()
                : roleCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizeRoleCode)
                .map(code -> code.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        if (normalizedRoleCodes.isEmpty()) {
            return List.of();
        }

        return rolePermissionRepository.findByNormalizedRoleCodeInAndIsDeleted(normalizedRoleCodes, Const.IN_DELETED)
                .stream()
                .collect(Collectors.groupingBy(
                        RolePermissions::getRoleCode,
                        Collectors.mapping(RolePermissions::getPermissionCode, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> RolePermissionDto.builder()
                        .role(entry.getKey())
                        .permissions(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RolePermissionDto> getAllPermissions() {
        return rolePermissionRepository.findAllByIsDeletedFalse()
                .stream()
                .collect(Collectors.groupingBy(
                        RolePermissions::getRoleCode,
                        Collectors.mapping(RolePermissions::getPermissionCode, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> RolePermissionDto.builder()
                        .role(entry.getKey())
                        .permissions(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RolePermissionDto> getPermissionWithCurrentUser(){
        // Role được lấy từ JWT hiện tại; database chỉ cung cấp permission tương ứng
        // với các role đó, nên kết quả phụ thuộc vào danh tính đang nằm trong request.
        List<String> roles = getRoles().stream().toList();
        return getPermissionsByRoleCodes(roles);
    }

    @Override
    @Transactional
    public void assignPermission(String roleCode, String permissionCode) {
        String role = normalizeRoleCode(roleCode);
        String permission = normalizePermissionCode(permissionCode);
        validateRole(role);
        validatePermission(permission);

        RolePermissions assignment = rolePermissionRepository.findByRoleCodeAndPermissionCode(role, permission)
                .orElseGet(() -> {
                    RolePermissions created = new RolePermissions();
                    created.setRoleCode(role);
                    created.setPermissionCode(permission);
                    return created;
                });
        if (Boolean.FALSE.equals(assignment.getIsDeleted())) {
            throw new BusinessException(DomainCode.CONFLICT, "quyền " + permission + " đã được gán cho vai trò " + role);
        }
        assignment.setIsDeleted(false);
        rolePermissionRepository.save(assignment);
    }

    @Override
    @Transactional
    public void updatePermissions(String roleCode, UpdateRolePermissionsRequest request) {
        String role = normalizeRoleCode(roleCode);
        validateRole(role);

        List<RolePermissions> assignments = rolePermissionRepository.findAllByRoleCodeIgnoreCase(role);

        Set<String> desired = request.getPermissionCodes() == null
                ? assignments.stream()
                .filter(item -> Boolean.FALSE.equals(item.getIsDeleted()))
                .map(RolePermissions::getPermissionCode)
                .map(this::normalizePermissionCode)
                .collect(Collectors.toCollection(HashSet::new))
                : normalizeCodes(request.getPermissionCodes());
        desired.addAll(normalizeCodes(request.getPermissionCodesToAdd()));
        desired.removeAll(normalizeCodes(request.getPermissionCodesToRemove()));

        validatePermissions(desired);
        Map<String, RolePermissions> assignmentsByPermission = assignments.stream()
                .collect(Collectors.toMap(
                        item -> normalizePermissionCode(item.getPermissionCode()),
                        Function.identity(),
                        (first, ignored) -> first));
        List<RolePermissions> changedAssignments = new ArrayList<>();

        for (String permission : desired) {
            RolePermissions assignment = assignmentsByPermission.get(permission);
            if (assignment == null) {
                assignment = new RolePermissions();
                assignment.setRoleCode(role);
                assignment.setPermissionCode(permission);
                assignment.setIsDeleted(false);
                changedAssignments.add(assignment);
            } else if (Boolean.TRUE.equals(assignment.getIsDeleted())) {
                assignment.setIsDeleted(false);
                changedAssignments.add(assignment);
            }
        }

        assignments.stream()
                .filter(item -> !desired.contains(normalizePermissionCode(item.getPermissionCode()))
                        && Boolean.FALSE.equals(item.getIsDeleted()))
                .forEach(item -> {
                    item.setIsDeleted(true);
                    changedAssignments.add(item);
                });
        if (!changedAssignments.isEmpty()) {
            rolePermissionRepository.saveAll(changedAssignments);
        }
    }

    @Override
    @Transactional
    public void deletePermission(String roleCode, String permissionCode) {
        String role = normalizeRoleCode(roleCode);
        String permission = normalizePermissionCode(permissionCode);
        validateRole(role);
        validatePermission(permission);
        RolePermissions assignment = rolePermissionRepository
                .findByRoleCodeAndPermissionCodeAndIsDeletedFalse(role, permission)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "quyền " + permission + " của vai trò " + role));
        assignment.setIsDeleted(true);
        rolePermissionRepository.save(assignment);
    }

    private void validateRole(String roleCode) {
        rolesRepository.findByRoleCodeIgnoreCaseAndIsDeletedFalse(roleCode)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "vai trò " + roleCode));
    }

    private void validatePermission(String permissionCode) {
        permissionsRepository.findByPermissionCodeIgnoreCaseAndIsDeletedFalse(permissionCode)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "quyền " + permissionCode));
    }

    private void validatePermissions(Set<String> permissionCodes) {
        if (permissionCodes.isEmpty()) {
            return;
        }
        Set<String> existingCodes = permissionsRepository
                .findAllByPermissionCodeInAndIsDeletedFalse(permissionCodes)
                .stream()
                .map(Permissions::getPermissionCode)
                .map(this::normalizePermissionCode)
                .collect(Collectors.toSet());
        if (!existingCodes.containsAll(permissionCodes)) {
            Set<String> missingCodes = new HashSet<>(permissionCodes);
            missingCodes.removeAll(existingCodes);
            throw new BusinessException(DomainCode.NOT_FOUND, "quyền " + String.join(", ", missingCodes));
        }
    }

    private String normalizeRoleCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePermissionCode(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private Set<String> normalizeCodes(List<String> values) {
        if (values == null) {
            return new HashSet<>();
        }
        return values.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizePermissionCode)
                .collect(Collectors.toCollection(HashSet::new));
    }

}
