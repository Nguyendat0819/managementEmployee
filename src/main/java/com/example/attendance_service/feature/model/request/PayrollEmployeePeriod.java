package com.example.attendance_service.feature.model.request;

import java.time.LocalDate;

public record PayrollEmployeePeriod(String employeeCode, LocalDate from, LocalDate to) {
}
