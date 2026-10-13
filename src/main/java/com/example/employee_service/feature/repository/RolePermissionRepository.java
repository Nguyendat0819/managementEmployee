package com.example.employee_service.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.example.employee_service.feature.entity.RolePermissions;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissions, Long> {
    List<RolePermissions> findByRoleCodeInAndIsDeleted(List<String> roleCodes, boolean IsDeleted);

    @Query("SELECT rp FROM RolePermissions rp WHERE LOWER(rp.roleCode) IN :roleCodes AND rp.isDeleted = :isDeleted")
    List<RolePermissions> findByNormalizedRoleCodeInAndIsDeleted(
            @Param("roleCodes") List<String> roleCodes,
            @Param("isDeleted") boolean isDeleted);

    List<RolePermissions> findAllByIsDeletedFalse();

    List<RolePermissions> findAllByRoleCodeAndIsDeletedFalse(String roleCode);

    List<RolePermissions> findAllByRoleCodeIgnoreCase(String roleCode);

    List<RolePermissions> findAllByPermissionCodeAndIsDeletedFalse(String permissionCode);

    Optional<RolePermissions> findByRoleCodeAndPermissionCode(String roleCode, String permissionCode);

    Optional<RolePermissions> findByRoleCodeAndPermissionCodeAndIsDeletedFalse(String roleCode, String permissionCode);

    long countByRoleCodeAndIsDeletedFalse(String roleCode);
}
