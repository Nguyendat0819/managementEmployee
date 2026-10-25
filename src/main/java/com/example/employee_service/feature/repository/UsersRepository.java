package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Users;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends BaseRepository<Users, Long> {
    boolean existsAllByEmailAndUserName(String email, String userName);

    List<Users> findAllByIsDeletedFalse();

    List<Users> findAllByIsDeletedFalseAndStatusOrderByUserNameAsc(String status);

    List<Users> findAllByEmailIgnoreCaseAndIsDeletedFalse(String email);

    Users findUserByUserName(String userName);

    Users findByUserNameAndIsDeletedFalse(String userName);

    boolean existsByUserNameIgnoreCaseAndIsDeletedFalse(String userName);

    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);

    long countByRoleCodeAndIsDeletedFalse(String roleCode);

    long countByRoleCodeAndStatusAndIsDeletedFalse(String roleCode, String status);
}
