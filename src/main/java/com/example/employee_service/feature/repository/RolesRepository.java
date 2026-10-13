package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Roles;
import com.example.employee_service.feature.repository.custom.RoleRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends BaseRepository<Roles, Long>, RoleRepositoryCustom {
    boolean existsByRoleCodeIgnoreCase(String roleCode);

    Optional<Roles> findByRoleCodeIgnoreCaseAndIsDeletedFalse(String roleCode);
}
