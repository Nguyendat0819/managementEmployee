package com.example.employee_service.feature.service.impl;


import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.Users;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    public final UsersRepository usersRepository;
    private final EmployeesRepository employeesRepository;

    @Override
    public PageResponse<UserResponse> getUsers(){
        List<UserResponse> response = usersRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::buildUser)
                .toList();

        return PageResponse.<UserResponse>builder()
                .content(response)
                .build();
    }


    private  UserResponse buildUser(Users user){
        String fullName = employeesRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(user.getEmail())
                .stream().findFirst().map(employee -> employee.getFullName())
                .orElse(user.getFullName() == null ? user.getUserName() : user.getFullName());
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .fullName(fullName)
                .roleCode(user.getRoleCode())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
