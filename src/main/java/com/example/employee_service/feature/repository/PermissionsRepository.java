package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Permissions;
import com.example.employee_service.feature.repository.custom.PermissionRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionsRepository extends BaseRepository<Permissions, Long>, PermissionRepositoryCustom {
    boolean existsByPermissionCodeIgnoreCase(String permissionCode);

    Optional<Permissions> findByPermissionCodeIgnoreCase(String permissionCode);

    Optional<Permissions> findByPermissionCodeIgnoreCaseAndIsDeletedFalse(String permissionCode);

    List<Permissions> findAllByPermissionCodeInAndIsDeletedFalse(Collection<String> permissionCodes);
}
