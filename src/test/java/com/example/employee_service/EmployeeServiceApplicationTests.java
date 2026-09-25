package com.example.employee_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;

/**
 * Verify Spring context khởi tạo thành công (không lỗi bean).
 *
 * <p>Test chạy với profile {@code test}, sử dụng:</p>
 * <ul>
 *   <li>H2 in-memory database (thay cho PostgreSQL)</li>
 *   <li>Mock {@link JwtDecoder} để không phụ thuộc Keycloak</li>
 *   <li>Issuer/JWK URI giả — chỉ để JwtDecoder bean không crash khi khởi tạo</li>
 * </ul>
 *
 * <p>Cấu hình DB & Keycloak cho profile test nằm trong {@code application-test.yaml}.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmployeeServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }
}