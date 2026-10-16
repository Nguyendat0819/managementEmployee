package com.example.payroll_service.feature.controller.api;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.security.RequiresAnyPermission;
import com.example.payroll_service.common.security.RequiresPermission;
import com.example.payroll_service.feature.model.request.SalaryStructureRequest;
import com.example.payroll_service.feature.model.response.SalaryStructureResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/salary-structures")
public interface SalaryStructureApi {
    @GetMapping
    @RequiresAnyPermission({"employee:salary:read", "employee:payroll:manage"})
    ApiResponse<List<SalaryStructureResponse>> getAll(@RequestParam(required = false) String search);

    @GetMapping("/{id}")
    @RequiresAnyPermission({"employee:salary:read", "employee:payroll:manage"})
    ApiResponse<SalaryStructureResponse> getById(@PathVariable Long id);

    @PostMapping
    @RequiresPermission("employee:salary:manage")
    ApiResponse<SalaryStructureResponse> create(@Valid @RequestBody SalaryStructureRequest request);

    @PutMapping("/{id}")
    @RequiresPermission("employee:salary:manage")
    ApiResponse<SalaryStructureResponse> update(@PathVariable Long id, @Valid @RequestBody SalaryStructureRequest request);

    @DeleteMapping("/{id}")
    @RequiresPermission("employee:salary:manage")
    ApiResponse<Void> delete(@PathVariable Long id);
}
