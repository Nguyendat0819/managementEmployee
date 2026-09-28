package com.example.employee_service.common.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình thông số Keycloak từ application.yaml (prefix: app.keycloak)
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.keycloak")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakProperties {

    String baseUrl = "http://localhost:8180";
    String realm = "hrManagement";
}
