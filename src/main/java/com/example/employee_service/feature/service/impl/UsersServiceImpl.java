package com.example.employee_service.feature.service.impl;


import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.constant.enums.UsersEnum;
import com.example.employee_service.feature.entity.users;
import com.example.employee_service.feature.mapper.UserMapper;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.model.response.UserResponse;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.KeycloakService;
import com.example.employee_service.feature.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

import static com.example.employee_service.common.util.Const.*;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    public final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;
    @Override
    public UserResponse create(UserCreateRequest request, String keycloakUserId){
        users user = new users();
        user.setEmail(request.getEmail());
        user.setUserName(request.getUsername());
        user.setKeycloakUserId(keycloakUserId);
        user.setRoleCode(request.getRoleCode());
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

        // lấy ra keycloakUserId
        String getUserKid = getUser.getKeycloakUserId();

        // Gỡ tài khoản
        if ("REVOKE".equalsIgnoreCase(UsersEnum.REVOKE.name())){
            // set trang thai không hoat dong
            getUser.setStatus(IS_INACTIVE);
            // Xử lý tắt hoạt động của keycloak
            keycloakService.updateUserEnabled(getUserKid,IS_INACTIVE_KEYCLOAK);
        }

        // Khôi phục tài khoản
        if("RESTORE".equalsIgnoreCase(UsersEnum.RESTORE.name())){
            getUser.setStatus(IS_ACTIVE);
            keycloakService.updateUserEnabled(getUserKid,IS_ACTIVE_KEYCLOAK);
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
