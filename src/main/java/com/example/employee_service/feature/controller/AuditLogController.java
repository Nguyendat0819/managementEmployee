package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.controller.api.AuditLogApi;
import com.example.employee_service.feature.model.response.AuditLogResponse;
import com.example.employee_service.feature.model.response.AuditLogSummaryResponse;
import com.example.employee_service.feature.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuditLogController implements AuditLogApi {

    private final AuditLogService auditLogService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<PageResponse<AuditLogResponse>> search(String search, String module, String action,
                                                                String result, LocalDate from, LocalDate to,
                                                                String[] sorts, int page, int size) {
        return responseFactory.success(auditLogService.search(
                search, module, action, result, from, to,
                sorts == null ? List.of() : Arrays.asList(sorts), page, size));
    }

    @Override
    public ApiResponse<AuditLogSummaryResponse> summary() {
        return responseFactory.success(auditLogService.summary());
    }
}
