package com.example.employee_service.common.persistence.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Cấu hình role -> danh sách permission code.
 * Dùng cho cache phân quyền (xem {@link com.example.employee_service.common.security.RolePermissionCache}).
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionDto {
    String role;
    List<String> permissions;
}