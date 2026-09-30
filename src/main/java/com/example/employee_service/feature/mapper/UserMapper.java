package com.example.employee_service.feature.mapper;

import com.example.employee_service.feature.entity.users;
import com.example.employee_service.feature.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "email", source = "email")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "roleCode", source = "roleCode")
    @Mapping(target = "status", source = "status")
    UserResponse toResponse(users entity);
}
