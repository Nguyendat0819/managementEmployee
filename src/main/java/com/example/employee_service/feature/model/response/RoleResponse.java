package com.example.employee_service.feature.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    Long id;
    String roleCode;
    String roleName;
    String description;
    Long userCount;
    Long permissionCount;
    Boolean isDeleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
