package com.example.payroll_service.feature.task.controller;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.security.RequiresPermission;
import com.example.payroll_service.feature.task.model.TaskRequest;
import com.example.payroll_service.feature.task.model.TaskResponse;
import com.example.payroll_service.feature.task.model.TaskReviewRequest;
import com.example.payroll_service.feature.task.model.TaskSubmissionRequest;
import com.example.payroll_service.feature.task.model.TaskProgressResponse;
import com.example.payroll_service.feature.task.model.MonthlyTaskKpiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/tasks")
public interface EmployeeTaskApi {
    @GetMapping
    @RequiresPermission("employee:task:manage")
    ApiResponse<List<TaskResponse>> getAll();

    @GetMapping("/kpi/monthly")
    @RequiresPermission("employee:task:manage")
    ApiResponse<List<MonthlyTaskKpiResponse>> getMonthlyKpis(@RequestParam LocalDate periodMonth);

    @GetMapping("/me/kpi/monthly")
    @RequiresPermission("employee:task:read")
    ApiResponse<MonthlyTaskKpiResponse> getMyMonthlyKpi(@RequestParam LocalDate periodMonth);

    @PostMapping
    @RequiresPermission("employee:task:manage")
    ApiResponse<TaskResponse> create(@Valid @RequestBody TaskRequest request);

    @PutMapping("/{id}")
    @RequiresPermission("employee:task:manage")
    ApiResponse<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request);

    @PostMapping("/{id}/approve")
    @RequiresPermission("employee:task:manage")
    ApiResponse<TaskResponse> approve(@PathVariable Long id, @Valid @RequestBody TaskReviewRequest request);

    @PostMapping("/{id}/reject")
    @RequiresPermission("employee:task:manage")
    ApiResponse<TaskResponse> reject(@PathVariable Long id, @Valid @RequestBody TaskReviewRequest request);

    @GetMapping("/{id}/history")
    @RequiresPermission("employee:task:manage")
    ApiResponse<List<TaskProgressResponse>> getHistory(@PathVariable Long id);

    @GetMapping("/me")
    @RequiresPermission("employee:task:read")
    ApiResponse<List<TaskResponse>> getMine();

    @GetMapping("/me/{id}/history")
    @RequiresPermission("employee:task:read")
    ApiResponse<List<TaskProgressResponse>> getMyHistory(@PathVariable Long id);

    @PostMapping("/me/{id}/start")
    @RequiresPermission("employee:task:read")
    ApiResponse<TaskResponse> startMine(@PathVariable Long id);

    @PostMapping(value = "/me/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresPermission("employee:task:read")
    ApiResponse<TaskResponse> submitMine(@PathVariable Long id,
                                         @RequestPart("submission") @Valid TaskSubmissionRequest request,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files);

    @GetMapping("/evidence/{fileKey}")
    @PreAuthorize("@permissionChecker.hasAny('employee:task:manage','employee:task:read')")
    ResponseEntity<Resource> getEvidence(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}_[A-Za-z0-9._-]{1,255}") String fileKey);
}
