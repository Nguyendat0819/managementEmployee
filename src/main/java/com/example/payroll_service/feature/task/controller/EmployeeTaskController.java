package com.example.payroll_service.feature.task.controller;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.ApiResponseFactory;
import com.example.payroll_service.feature.task.model.TaskRequest;
import com.example.payroll_service.feature.task.model.TaskResponse;
import com.example.payroll_service.feature.task.model.TaskReviewRequest;
import com.example.payroll_service.feature.task.model.TaskSubmissionRequest;
import com.example.payroll_service.feature.task.model.TaskProgressResponse;
import com.example.payroll_service.feature.task.model.MonthlyTaskKpiResponse;
import com.example.payroll_service.feature.task.service.EmployeeTaskService;
import com.example.payroll_service.feature.task.service.TaskEvidenceStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class EmployeeTaskController implements EmployeeTaskApi {
    private final EmployeeTaskService service;
    private final ApiResponseFactory responseFactory;
    private final TaskEvidenceStorage evidenceStorage;

    @Override
    public ApiResponse<List<TaskResponse>> getAll() {
        return responseFactory.success(service.getAll());
    }

    @Override
    public ApiResponse<List<MonthlyTaskKpiResponse>> getMonthlyKpis(LocalDate periodMonth) {
        return responseFactory.success(service.getMonthlyKpis(periodMonth));
    }

    @Override
    public ApiResponse<MonthlyTaskKpiResponse> getMyMonthlyKpi(LocalDate periodMonth) {
        return responseFactory.success(service.getMyMonthlyKpi(periodMonth));
    }

    @Override
    public ApiResponse<TaskResponse> create(TaskRequest request) {
        return responseFactory.success(service.save(null, request));
    }

    @Override
    public ApiResponse<TaskResponse> update(Long id, TaskRequest request) {
        return responseFactory.success(service.save(id, request));
    }

    @Override
    public ApiResponse<TaskResponse> approve(Long id, TaskReviewRequest request) {
        return responseFactory.success(service.approve(id, request));
    }

    @Override
    public ApiResponse<TaskResponse> reject(Long id, TaskReviewRequest request) {
        return responseFactory.success(service.reject(id, request));
    }

    @Override
    public ApiResponse<List<TaskProgressResponse>> getHistory(Long id) {
        return responseFactory.success(service.getHistory(id));
    }

    @Override
    public ApiResponse<List<TaskResponse>> getMine() {
        return responseFactory.success(service.getMine());
    }

    @Override
    public ApiResponse<List<TaskProgressResponse>> getMyHistory(Long id) {
        return responseFactory.success(service.getMyHistory(id));
    }

    @Override
    public ApiResponse<TaskResponse> startMine(Long id) {
        return responseFactory.success(service.startMine(id));
    }

    @Override
    public ApiResponse<TaskResponse> submitMine(Long id, TaskSubmissionRequest request, List<MultipartFile> files) {
        String publicBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return responseFactory.success(service.submitMine(id, request, files, publicBaseUrl));
    }

    @Override
    public ResponseEntity<Resource> getEvidence(String fileKey) {
        TaskEvidenceStorage.DownloadableEvidence evidence = evidenceStorage.load(fileKey);
        String disposition = evidence.isImage() ? "inline" : "attachment";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(evidence.contentType() == null ? "application/octet-stream" : evidence.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder(disposition).filename(evidence.fileName()).build().toString())
                .body(evidence.resource());
    }
}
