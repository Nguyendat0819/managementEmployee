package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.feature.entity.Users;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends BaseRepository<Users, Long> {
    boolean existsAllByEmailAndUserName(String email, String userName);

    List<Users> findAllByIsDeletedFalse();

    Users findUserByUserName(String userName);

    Users findByUserNameAndIsDeletedFalse(String userName);
}
