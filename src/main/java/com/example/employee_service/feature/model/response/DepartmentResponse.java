package com.example.employee_service.feature.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentResponse {
    Long id;
    String deptCode;
    String parentCode;
    String name;
    String code;
    String headCode;
    Short level;
    Boolean isDeleted;
    long employeeCount;
}
