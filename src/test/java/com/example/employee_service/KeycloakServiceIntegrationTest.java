package com.example.employee_service;

import com.example.employee_service.feature.model.dto.KeycloakUserRepresentation;
import com.example.employee_service.feature.model.request.UserCreateRequest;
import com.example.employee_service.feature.service.KeycloakService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class KeycloakServiceIntegrationTest {

    @Autowired
    KeycloakService keycloakService;

    @Test
    void testCreateGetAndDeleteUser() {
        String testUsername = "auto_test_user_" + System.currentTimeMillis();
        String testEmail = testUsername + "@example.com";

        UserCreateRequest request = UserCreateRequest.builder()
                .username(testUsername)
                .email(testEmail)
                .firstName("Auto")
                .lastName("Tester")
                .password("Password123!")
                .roleCode("USER")
                .build();

        // 1. Tạo user trên Keycloak
        String userId = keycloakService.createUser(request);
        assertNotNull(userId, "UserId không được rỗng");

        try {
            // 2. Lấy thông tin user vừa tạo
            KeycloakUserRepresentation user = keycloakService.getUserById(userId);
            assertNotNull(user);
            assertEquals(testUsername, user.getUsername());
            assertEquals(testEmail, user.getEmail());

        } finally {
            // 3. Xoá user để dọn dẹp môi trường test
            keycloakService.deleteUser(userId);
        }
    }
}
