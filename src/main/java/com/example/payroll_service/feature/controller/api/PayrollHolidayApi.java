package com.example.payroll_service.feature.controller.api;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.security.RequiresPermission;
import com.example.payroll_service.feature.model.request.PayrollHolidayRequest;
import com.example.payroll_service.feature.model.response.PayrollHolidayResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/payroll-holidays")
@RequiresPermission("employee:payroll:manage")
public interface PayrollHolidayApi {
    @GetMapping
    ApiResponse<List<PayrollHolidayResponse>> getAll();

    @PostMapping
    ApiResponse<PayrollHolidayResponse> create(@Valid @RequestBody PayrollHolidayRequest request);

    @PutMapping("/{id}")
    ApiResponse<PayrollHolidayResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody PayrollHolidayRequest request);

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id);
}
