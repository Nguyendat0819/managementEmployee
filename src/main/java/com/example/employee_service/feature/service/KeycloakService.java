package com.example.employee_service.feature.service;

import com.example.employee_service.feature.model.dto.KeycloakUserRepresentation;
import com.example.employee_service.feature.model.request.UserCreateRequest;

public interface KeycloakService {

    /**
     * Tạo tài khoản người dùng trên Keycloak qua Admin REST API.
     *
     * @param request Thông tin tài khoản cần tạo
     * @return ID người dùng trong Keycloak (UUID)
     */
    String createUser(UserCreateRequest request);

    /**
     * Lấy thông tin người dùng từ Keycloak theo ID.
     */
    KeycloakUserRepresentation getUserById(String keycloakUserId);

    /**
     * Gán Realm Role cho người dùng trên Keycloak.
     */
    void assignRealmRole(String keycloakUserId, String roleName);

    /**
     * Xoá người dùng khỏi Keycloak (dùng khi rollback).
     */
    void deleteUser(String keycloakUserId);
}
