package com.example.leave_service.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.example.leave_service.feature.entity.RolePermissions;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissions, Long> {
    List<RolePermissions> findByRoleCodeInAndIsDeleted(List<String> roleCodes, boolean IsDeleted);

    List<RolePermissions> findAllByIsDeletedFalse();
}
