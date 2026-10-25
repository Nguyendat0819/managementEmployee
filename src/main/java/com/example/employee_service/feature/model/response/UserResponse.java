package com.example.employee_service.feature.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String email;
    String userName;
    String fullName;
    String roleCode;
    String status;
    java.time.LocalDateTime lastLoginAt;
    java.time.LocalDateTime createdAt;
}
