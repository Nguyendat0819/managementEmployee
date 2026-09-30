package com.example.employee_service.feature.repository;

import com.example.employee_service.common.persistence.BaseRepository;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.users;
import com.example.employee_service.feature.model.response.UserResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends BaseRepository<users, Long> {
    boolean existsAllByEmailAndUserName(String email, String userName);

    List<users> findAllByIsDeletedFalse();

    users findUserByUserName(String userName);
}