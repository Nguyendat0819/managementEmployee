package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.feature.entity.RolePermissions;
import com.example.employee_service.feature.entity.Roles;
import com.example.employee_service.feature.model.request.RoleCreateRequest;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.repository.RolePermissionRepository;
import com.example.employee_service.feature.repository.RolesRepository;
import com.example.employee_service.feature.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @Mock(mockMaker = "mock-maker-subclass") PositionsRepository positionsRepository;
    @Mock(mockMaker = "mock-maker-subclass") RolesRepository rolesRepository;
    @Mock(mockMaker = "mock-maker-subclass") UsersRepository usersRepository;
    @Mock(mockMaker = "mock-maker-subclass") RolePermissionRepository rolePermissionRepository;
    @InjectMocks RoleServiceImpl service;

    @Test
    void createNormalizesRoleCodeBeforeSaving() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setRoleCode(" hr_manager ");
        request.setRoleName("Quản lý nhân sự");
        when(rolesRepository.save(any(Roles.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createRole(request);

        assertEquals("HR_MANAGER", response.getRoleCode());
        verify(rolesRepository).existsByRoleCodeIgnoreCase("HR_MANAGER");
        verify(rolesRepository).save(any(Roles.class));
    }

    @Test
    void deleteRejectsRoleAssignedToActiveUser() {
        Roles role = activeRole("HR_MANAGER");
        when(rolesRepository.findById(7L)).thenReturn(Optional.of(role));
        when(usersRepository.countByRoleCodeAndIsDeletedFalse("HR_MANAGER")).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.deleteRole(7L));

        verify(rolesRepository, never()).save(any());
    }

    @Test
    void deleteSoftDeletesRoleAndItsPermissions() {
        Roles role = activeRole("HR_MANAGER");
        RolePermissions permission = new RolePermissions();
        permission.setRoleCode("HR_MANAGER");
        permission.setPermissionCode("employee.read");
        permission.setIsDeleted(false);
        when(rolesRepository.findById(7L)).thenReturn(Optional.of(role));
        when(rolePermissionRepository.findAllByRoleCodeAndIsDeletedFalse("HR_MANAGER"))
                .thenReturn(List.of(permission));

        service.deleteRole(7L);

        assertTrue(role.getIsDeleted());
        assertTrue(permission.getIsDeleted());
        verify(rolesRepository).save(role);
        verify(rolePermissionRepository).saveAll(List.of(permission));
    }

    private Roles activeRole(String roleCode) {
        Roles role = new Roles();
        role.setRoleCode(roleCode);
        role.setRoleName("Quản lý nhân sự");
        role.setIsDeleted(false);
        return role;
    }
}
