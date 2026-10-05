package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.common.util.Const;
import com.example.employee_service.feature.entity.RolePermissions;
import com.example.employee_service.feature.repository.RolePermissionRepository;
import com.example.employee_service.feature.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.employee_service.common.util.SecurityUtil.getRoles;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionServiceImpl implements RolePermissionService {
    RolePermissionRepository rolePermissionRepository;
    @Override
    public List<RolePermissionDto> getPermissionsByRoleCodes(List<String> roleCodes) {
        return rolePermissionRepository.findByRoleCodeInAndIsDeleted(roleCodes, Const.IN_DELETED)
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

}
