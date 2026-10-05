package com.example.employee_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verify Spring context khởi động được với profile {@code dev}
 * (H2 in-memory + security tắt theo application-dev.yaml).
 *
 * <p>Mục tiêu: bắt lỗi "Failed to configure a DataSource" sớm trong CI
 * mà không cần dựng PostgreSQL thật.</p>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:devtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS hr_service",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database=h2",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@ActiveProfiles("dev")
class DevProfileApplicationTests {

    @Test
    void contextLoads() {
    }
}
