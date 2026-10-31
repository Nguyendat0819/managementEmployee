package com.example.employee_service.feature.controller.api;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.common.security.RequiresPermission;
import com.example.employee_service.feature.constant.PermissionDefine;
import com.example.employee_service.feature.model.response.AuditLogResponse;
import com.example.employee_service.feature.model.response.AuditLogSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
@RequestMapping("/api/audit-logs")
public interface AuditLogApi {

    @GetMapping
    @RequiresPermission(PermissionDefine.AUDIT_LOG_READ)
    ApiResponse<PageResponse<AuditLogResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String[] sorts,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/summary")
    @RequiresPermission(PermissionDefine.AUDIT_LOG_READ)
    ApiResponse<AuditLogSummaryResponse> summary();
}
