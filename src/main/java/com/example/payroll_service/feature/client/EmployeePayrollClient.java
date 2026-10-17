package com.example.payroll_service.feature.client;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.feature.model.response.PayrollEmployeeResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.List;

@HttpExchange
public interface EmployeePayrollClient {
    @GetExchange("/employee-service/api/internal/payroll/employees")
    ApiResponse<List<PayrollEmployeeResponse>> getEligibleEmployees(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);
}
