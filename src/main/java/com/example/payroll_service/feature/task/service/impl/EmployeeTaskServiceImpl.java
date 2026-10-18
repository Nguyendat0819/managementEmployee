package com.example.payroll_service.feature.task.service.impl;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.exception.ResourceNotFoundException;
import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.DomainCode;
import com.example.payroll_service.common.util.SecurityUtil;
import com.example.payroll_service.feature.client.AdminManagerClient;
import com.example.payroll_service.feature.model.response.EmployeeProfileResponse;
import com.example.payroll_service.feature.task.entity.EmployeeTask;
import com.example.payroll_service.feature.task.entity.EmployeeTaskProgress;
import com.example.payroll_service.feature.task.model.MonthlyTaskKpiResponse;
import com.example.payroll_service.feature.task.model.TaskProgressResponse;
import com.example.payroll_service.feature.task.model.TaskRequest;
import com.example.payroll_service.feature.task.model.TaskResponse;
import com.example.payroll_service.feature.task.model.TaskReviewRequest;
import com.example.payroll_service.feature.task.model.TaskSubmissionRequest;
import com.example.payroll_service.feature.task.repository.EmployeeTaskProgressRepository;
import com.example.payroll_service.feature.task.repository.EmployeeTaskRepository;
import com.example.payroll_service.feature.task.service.EmployeeTaskService;
import com.example.payroll_service.feature.task.service.TaskEvidenceStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeTaskServiceImpl implements EmployeeTaskService {
    private static final String TODO = "TODO";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String SUBMITTED = "SUBMITTED";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String PENDING = "PENDING";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    EmployeeTaskRepository repository;
    EmployeeTaskProgressRepository progressRepository;
    AdminManagerClient employeeClient;
    TaskEvidenceStorage evidenceStorage;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAll() {
        return toResponses(repository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMine() {
        return toResponses(repository.findAllByEmployeeCodeIgnoreCaseOrderByDueDateAscCreatedAtDesc(currentEmployeeCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTaskKpiResponse> getMonthlyKpis(LocalDate periodMonth) {
        List<EmployeeTask> tasks = repository.findAllByPeriodMonthOrderByEmployeeCodeAscCreatedAtDesc(firstDayOfMonth(periodMonth));
        Map<String, List<EmployeeTask>> groups = tasks.stream().collect(Collectors.groupingBy(
                task -> task.getEmployeeCode().toLowerCase(Locale.ROOT), TreeMap::new, Collectors.toList()));
        return groups.values().stream()
                .map(employeeTasks -> calculateMonthlyKpi(employeeTasks.get(0).getEmployeeCode(), periodMonth, employeeTasks))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyTaskKpiResponse getMyMonthlyKpi(LocalDate periodMonth) {
        String employeeCode = currentEmployeeCode();
        List<EmployeeTask> tasks = repository.findAllByPeriodMonthOrderByEmployeeCodeAscCreatedAtDesc(firstDayOfMonth(periodMonth))
                .stream().filter(task -> task.getEmployeeCode().equalsIgnoreCase(employeeCode)).toList();
        return calculateMonthlyKpi(employeeCode, periodMonth, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskProgressResponse> getHistory(Long id) {
        requireTask(id);
        return progressRepository.findAllByTaskIdOrderBySubmittedAtDesc(id).stream().map(this::toProgressResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskProgressResponse> getMyHistory(Long id) {
        findMine(id);
        return progressRepository.findAllByTaskIdOrderBySubmittedAtDesc(id).stream().map(this::toProgressResponse).toList();
    }

    @Override
    @Transactional
    public TaskResponse save(Long id, TaskRequest request) {
        LocalDate periodMonth = firstDayOfMonth(request.periodMonth());
        EmployeeTask task = id == null ? new EmployeeTask() : requireTask(id);
        if (id != null && !TODO.equals(task.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "chỉ có thể sửa task chưa bắt đầu");
        }
        task.setEmployeeCode(request.employeeCode().trim());
        task.setTitle(request.title().trim());
        task.setDescription(request.description() == null ? "" : request.description().trim());
        task.setAcceptanceCriteria(request.acceptanceCriteria().trim());
        task.setPeriodMonth(periodMonth);
        task.setDueDate(request.dueDate());
        task.setWeight(request.weight().setScale(2, RoundingMode.HALF_UP));
        task.setBonusAmount(request.bonusAmount().setScale(2, RoundingMode.HALF_UP));
        task.setPenaltyAmount(request.penaltyAmount().setScale(2, RoundingMode.HALF_UP));
        if (id == null) task.setStatus(TODO);
        return toResponse(repository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse startMine(Long id) {
        EmployeeTask task = findMine(id);
        if (!TODO.equals(task.getStatus()) && !REJECTED.equals(task.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "task chưa thể bắt đầu ở trạng thái hiện tại");
        }
        task.setStatus(IN_PROGRESS);
        return toResponse(repository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse submitMine(Long id, TaskSubmissionRequest request, List<MultipartFile> files, String publicBaseUrl) {
        EmployeeTask task = findMine(id);
        if (!IN_PROGRESS.equals(task.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "chỉ có thể nộp task đang thực hiện");
        }
        List<String> evidenceLinks = new ArrayList<>(request.evidenceLinks() == null ? List.of() : request.evidenceLinks());
        evidenceLinks.addAll(evidenceStorage.store(files, publicBaseUrl));
        if (evidenceLinks.isEmpty()) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "cần ít nhất một link hoặc file minh chứng");
        }
        LocalDateTime submittedAt = LocalDateTime.now(BUSINESS_ZONE);
        EmployeeTaskProgress progress = new EmployeeTaskProgress();
        progress.setTaskId(task.getId());
        progress.setResult(request.result().trim());
        progress.setEvidenceLinks(List.copyOf(evidenceLinks));
        progress.setStatus(PENDING);
        progress.setSubmittedAt(submittedAt);
        progressRepository.save(progress);
        task.setStatus(SUBMITTED);
        task.setSubmittedAt(submittedAt);
        task.setReviewedAt(null);
        task.setReviewedBy(null);
        task.setReviewComment(null);
        return toResponse(repository.save(task), progress);
    }

    @Override
    @Transactional
    public TaskResponse approve(Long id, TaskReviewRequest request) {
        EmployeeTask task = findPendingReview(id);
        EmployeeTaskProgress progress = findPendingProgress(task.getId());
        if (progress.getEvidenceLinks() == null || progress.getEvidenceLinks().isEmpty()) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "task cần có ít nhất một bằng chứng trước khi nghiệm thu");
        }
        LocalDateTime reviewedAt = LocalDateTime.now(BUSINESS_ZONE);
        String reviewer = SecurityUtil.getCurrentUsername();
        String comment = request == null || request.comment() == null ? "" : request.comment().trim();
        progress.setStatus(APPROVED);
        progress.setReviewedAt(reviewedAt);
        progress.setReviewedBy(reviewer);
        progress.setReviewComment(comment);
        task.setStatus(APPROVED);
        task.setReviewedAt(reviewedAt);
        task.setReviewedBy(reviewer);
        task.setReviewComment(comment);
        progressRepository.save(progress);
        return toResponse(repository.save(task), progress);
    }

    @Override
    @Transactional
    public TaskResponse reject(Long id, TaskReviewRequest request) {
        EmployeeTask task = findPendingReview(id);
        if (request == null || !request.hasReason()) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "cần nhập lý do yêu cầu bổ sung");
        }
        EmployeeTaskProgress progress = findPendingProgress(task.getId());
        LocalDateTime reviewedAt = LocalDateTime.now(BUSINESS_ZONE);
        String reviewer = SecurityUtil.getCurrentUsername();
        String reason = request.comment().trim();
        progress.setStatus(REJECTED);
        progress.setReviewedAt(reviewedAt);
        progress.setReviewedBy(reviewer);
        progress.setReviewComment(reason);
        task.setStatus(REJECTED);
        task.setReviewedAt(reviewedAt);
        task.setReviewedBy(reviewer);
        task.setReviewComment(reason);
        progressRepository.save(progress);
        return toResponse(repository.save(task), progress);
    }

    private EmployeeTask findMine(Long id) {
        return repository.findByIdAndEmployeeCodeIgnoreCase(id, currentEmployeeCode())
                .orElseThrow(() -> new ResourceNotFoundException("task " + id));
    }

    private EmployeeTask requireTask(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task " + id));
    }

    private EmployeeTask findPendingReview(Long id) {
        EmployeeTask task = requireTask(id);
        if (!SUBMITTED.equals(task.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "task chưa được nộp để duyệt");
        }
        return task;
    }

    private EmployeeTaskProgress findPendingProgress(Long taskId) {
        EmployeeTaskProgress progress = progressRepository.findFirstByTaskIdOrderBySubmittedAtDesc(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("bằng chứng task " + taskId));
        if (!PENDING.equals(progress.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "bằng chứng task không còn chờ xác minh");
        }
        return progress;
    }

    private String currentEmployeeCode() {
        try {
            ApiResponse<EmployeeProfileResponse> response = employeeClient.getCurrentEmployee();
            String employeeCode = response == null || response.getData() == null
                    ? null : response.getData().employeeCode();
            if (employeeCode == null || employeeCode.isBlank()) {
                throw new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại");
            }
            return employeeCode.trim();
        } catch (RestClientException exception) {
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "employee profile");
        }
    }

    private LocalDate firstDayOfMonth(LocalDate periodMonth) {
        if (periodMonth == null || periodMonth.getDayOfMonth() != 1) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "periodMonth phải là ngày đầu tháng");
        }
        return periodMonth;
    }

    private List<TaskResponse> toResponses(List<EmployeeTask> tasks) {
        if (tasks.isEmpty()) return List.of();
        List<Long> taskIds = tasks.stream().map(EmployeeTask::getId).toList();
        Map<Long, EmployeeTaskProgress> latestByTask = new LinkedHashMap<>();
        progressRepository.findAllByTaskIdInOrderBySubmittedAtDesc(taskIds)
                .forEach(progress -> latestByTask.putIfAbsent(progress.getTaskId(), progress));
        return tasks.stream().map(task -> toResponse(task, latestByTask.get(task.getId()))).toList();
    }

    private TaskResponse toResponse(EmployeeTask task) {
        EmployeeTaskProgress latest = progressRepository.findFirstByTaskIdOrderBySubmittedAtDesc(task.getId()).orElse(null);
        return toResponse(task, latest);
    }

    private TaskResponse toResponse(EmployeeTask task, EmployeeTaskProgress latestProgress) {
        BigDecimal bonus = task.getBonusAmount() == null ? BigDecimal.ZERO : task.getBonusAmount();
        BigDecimal penalty = task.getPenaltyAmount() == null ? BigDecimal.ZERO : task.getPenaltyAmount();
        return new TaskResponse(task.getId(), task.getEmployeeCode(), task.getTitle(), task.getDescription(),
                task.getAcceptanceCriteria(), task.getPeriodMonth(), task.getDueDate(), task.getWeight(), bonus,
                penalty, task.getStatus(), task.getSubmittedAt(), task.getReviewedAt(),
                task.getReviewedBy(), task.getReviewComment(), latestProgress == null ? null : toProgressResponse(latestProgress));
    }

    private TaskProgressResponse toProgressResponse(EmployeeTaskProgress progress) {
        return new TaskProgressResponse(progress.getId(), progress.getResult(),
                progress.getEvidenceLinks() == null ? List.of() : List.copyOf(progress.getEvidenceLinks()), progress.getStatus(),
                progress.getSubmittedAt(), progress.getReviewedAt(), progress.getReviewedBy(), progress.getReviewComment());
    }

    private MonthlyTaskKpiResponse calculateMonthlyKpi(String employeeCode, LocalDate periodMonth, List<EmployeeTask> tasks) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal completedWeight = BigDecimal.ZERO;
        long completedCount = 0;
        long pendingCount = 0;
        for (EmployeeTask task : tasks) {
            BigDecimal weight = task.getWeight() == null ? BigDecimal.ONE : task.getWeight();
            totalWeight = totalWeight.add(weight);
            if (APPROVED.equals(task.getStatus())) {
                completedWeight = completedWeight.add(weight);
                completedCount++;
            }
            if (SUBMITTED.equals(task.getStatus())) pendingCount++;
        }
        return new MonthlyTaskKpiResponse(employeeCode, firstDayOfMonth(periodMonth), tasks.size(), completedCount,
                tasks.size() - completedCount, pendingCount, completedWeight.setScale(2, RoundingMode.HALF_UP),
                totalWeight.setScale(2, RoundingMode.HALF_UP));
    }
}
