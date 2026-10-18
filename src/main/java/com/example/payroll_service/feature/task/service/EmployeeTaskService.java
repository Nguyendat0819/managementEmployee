package com.example.payroll_service.feature.task.service;

import com.example.payroll_service.feature.task.model.TaskRequest;
import com.example.payroll_service.feature.task.model.TaskResponse;
import com.example.payroll_service.feature.task.model.TaskReviewRequest;
import com.example.payroll_service.feature.task.model.TaskSubmissionRequest;
import com.example.payroll_service.feature.task.model.TaskProgressResponse;
import com.example.payroll_service.feature.task.model.MonthlyTaskKpiResponse;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeTaskService {
    List<TaskResponse> getAll();
    List<TaskResponse> getMine();
    List<MonthlyTaskKpiResponse> getMonthlyKpis(LocalDate periodMonth);
    MonthlyTaskKpiResponse getMyMonthlyKpi(LocalDate periodMonth);
    List<TaskProgressResponse> getHistory(Long id);
    List<TaskProgressResponse> getMyHistory(Long id);
    TaskResponse save(Long id, TaskRequest request);
    TaskResponse startMine(Long id);
    TaskResponse submitMine(Long id, TaskSubmissionRequest request, List<MultipartFile> files, String publicBaseUrl);
    TaskResponse approve(Long id, TaskReviewRequest request);
    TaskResponse reject(Long id, TaskReviewRequest request);
}
