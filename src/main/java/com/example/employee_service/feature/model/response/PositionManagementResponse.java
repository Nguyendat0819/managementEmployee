package com.example.employee_service.feature.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PositionManagementResponse {
    Long id;
    String positionCode;
    String title;
    String level;
    BigDecimal baseSalaryMin;
    BigDecimal baseSalaryMax;
    Long employeeCount;
    Boolean isDeleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
