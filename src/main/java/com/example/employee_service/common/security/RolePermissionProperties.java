package com.example.employee_service.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class RolePermissionProperties {

    private Map<String, List<String>> rolePermissions = new LinkedHashMap<>();
}
