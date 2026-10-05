package com.example.employee_service.feature.service.impl;


import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.entity.users;
import com.example.employee_service.feature.mapper.UserMapper;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;

import static com.example.employee_service.common.util.Const.*;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    public final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponse create(UserCreateRequest request){
        users user = new users();
        user.setEmail(request.getEmail());
        user.setUserName(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleCode(request.getRoleCode() == null || request.getRoleCode().isBlank() ? "USER" : request.getRoleCode());
        user.setStatus(IS_ACTIVE);
        user.setIsDeleted(false);

        users saved = usersRepository.save(user);

        return buildUser(saved);
    }

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


    @Override
    public UserResponse updateUserStatus(String userName, UsersEnum action) {
        users getUser = usersRepository.findUserByUserName(userName);

        if (action == UsersEnum.REVOKE) {
            getUser.setStatus(IS_INACTIVE);
        }

        if (action == UsersEnum.RESTORE) {
            getUser.setStatus(IS_ACTIVE);
        }

        usersRepository.save(getUser);
        return userMapper.toResponse(getUser);

    }
    private  UserResponse buildUser(users user){
        return UserResponse.builder()
                .email(user.getEmail())
                .userName(user.getUserName())
                .roleCode(user.getRoleCode())
                .status(user.getStatus())
                .build();
    }
}
