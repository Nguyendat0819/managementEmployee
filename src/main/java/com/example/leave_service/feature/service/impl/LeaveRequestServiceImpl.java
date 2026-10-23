package com.example.leave_service.feature.service.impl;

import com.example.leave_service.common.exception.BusinessException;
import com.example.leave_service.common.exception.ResourceNotFoundException;
import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.response.DomainCode;
import com.example.leave_service.common.response.PageResponse;
import com.example.leave_service.feature.entity.Holiday;
import com.example.leave_service.feature.entity.LeaveBalance;
import com.example.leave_service.feature.entity.LeaveRequest;
import com.example.leave_service.feature.entity.LeaveType;
import com.example.leave_service.feature.client.AdminManagerClient;
import com.example.leave_service.feature.model.LeaveRequestStatus;
import com.example.leave_service.feature.model.request.CreateLeaveRequestRequest;
import com.example.leave_service.feature.model.request.LeaveDecisionRequest;
import com.example.leave_service.feature.model.response.EmployeeProfileResponse;
import com.example.leave_service.feature.model.response.LeaveBalanceResponse;
import com.example.leave_service.feature.model.response.LeaveRequestResponse;
import com.example.leave_service.feature.repository.LeaveBalanceRepository;
import com.example.leave_service.feature.repository.LeaveHolidayRepository;
import com.example.leave_service.feature.repository.LeaveRequestRepository;
import com.example.leave_service.feature.repository.LeaveTypeRepository;
import com.example.leave_service.feature.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.example.leave_service.feature.model.LeaveRequestStatus.APPROVED;
import static com.example.leave_service.feature.model.LeaveRequestStatus.CANCELLED;
import static com.example.leave_service.feature.model.LeaveRequestStatus.PENDING;
import static com.example.leave_service.feature.model.LeaveRequestStatus.REJECTED;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private static final List<String> ACTIVE_REQUEST_STATUSES = List.of(PENDING, APPROVED);

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveHolidayRepository holidayRepository;
    private final AdminManagerClient employeeClient;

    @Override
    @Transactional
    public LeaveRequestResponse create(CreateLeaveRequestRequest request, Long userId) {
        String employeeCode = currentEmployeeCode();
        validateDateRange(request.startDate(), request.endDate());
        LeaveType leaveType = findActiveLeaveType(request.leaveTypeId());
        if (Boolean.TRUE.equals(leaveType.getRequireDocument())
                && (request.evidenceUrl() == null || request.evidenceUrl().isBlank())) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Vui lòng đính kèm minh chứng");
        }

        BigDecimal requestedDays = calculateWorkingDays(request.startDate(), request.endDate());
        if (requestedDays.signum() <= 0) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Khoảng nghỉ không có ngày làm việc");
        }

        List<LeaveRequest> overlapping = leaveRequestRepository
                .findByRequesterUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
                        userId, request.endDate(), request.startDate(), ACTIVE_REQUEST_STATUSES);
        if (!overlapping.isEmpty()) {
            throw new BusinessException(DomainCode.CONFLICT, "Thời gian nghỉ trùng với đơn đang chờ hoặc đã duyệt");
        }

        int year = request.startDate().getYear();
        LeaveBalance balance = findOrCreateBalance(userId, employeeCode, leaveType, year);
        BigDecimal pendingDays = pendingDays(employeeCode, leaveType.getId(), year, null);
        ensureAvailable(approvedDays(employeeCode, leaveType.getId(), year),
                pendingDays, requestedDays, balance.getTotalDays());

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setRequestCode("NP-" + year + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        leaveRequest.setRequesterUserId(userId);
        leaveRequest.setEmployeeCode(employeeCode);
        leaveRequest.setType(leaveType.getId());
        leaveRequest.setStartDate(request.startDate());
        leaveRequest.setEndDate(request.endDate());
        leaveRequest.setTotalDays(requestedDays);
        leaveRequest.setReason(request.reason().trim());
        leaveRequest.setEvidenceUrl(blankToNull(request.evidenceUrl()));
        leaveRequest.setStatus(PENDING);
        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return LeaveRequestResponse.from(leaveRequest, leaveType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getMine(Long userId) {
        return leaveRequestRepository.findByRequesterUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancel(Long id, Long userId) {
        LeaveRequest leaveRequest = findRequestForUpdate(id);
        if (!userId.equals(leaveRequest.getRequesterUserId())) {
            throw new ResourceNotFoundException("Đơn nghỉ phép", id);
        }
        if (!PENDING.equals(leaveRequest.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "Chỉ có thể hủy đơn đang chờ duyệt");
        }
        leaveRequest.setStatus(CANCELLED);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> search(String status, String employeeCode,
                                                      LocalDate from, LocalDate to, int page, int size) {
        validateSearchRange(from, to);
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        Specification<LeaveRequest> specification = (root, query, builder) -> builder.conjunction();

        if (status != null && !status.isBlank()) {
            String statusCode = normalizeStatus(status);
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), statusCode));
        }
        if (employeeCode != null && !employeeCode.isBlank()) {
            String employeeFilter = "%" + employeeCode.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("employeeCode")), employeeFilter));
        }
        if (from != null) {
            specification = specification.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("endDate"), from));
        }
        if (to != null) {
            specification = specification.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("startDate"), to));
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LeaveRequest> requests = leaveRequestRepository.findAll(specification, pageable);
        return PageResponse.of(requests.map(this::toResponse));
    }

    @Override
    @Transactional
    public LeaveRequestResponse approve(Long id, LeaveDecisionRequest decision) {
        String approverCode = currentEmployeeCode();
        LeaveRequest leaveRequest = findRequestForUpdate(id);
        requirePending(leaveRequest);
        LeaveType leaveType = leaveTypeRepository.findByIdForUpdate(leaveRequest.getType())
                .orElseThrow(() -> new ResourceNotFoundException("Loại nghỉ phép", leaveRequest.getType()));
        int year = leaveRequest.getStartDate().getYear();
        LeaveBalance balance = findOrCreateBalance(leaveRequest.getRequesterUserId(), leaveRequest.getEmployeeCode(), leaveType, year);
        BigDecimal approved = approvedDays(leaveRequest.getEmployeeCode(), leaveType.getId(), year);
        BigDecimal otherPending = pendingDays(leaveRequest.getEmployeeCode(), leaveType.getId(), year, leaveRequest.getId());
        ensureAvailable(approved, otherPending, leaveRequest.getTotalDays(), balance.getTotalDays());

        balance.setUsedDays(approved.add(leaveRequest.getTotalDays()));
        leaveBalanceRepository.save(balance);
        leaveRequest.setStatus(APPROVED);
        leaveRequest.setApproverCode(approverCode);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setRejectReason(blankToNull(decision == null ? null : decision.reason()));
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional
    public LeaveRequestResponse reject(Long id, LeaveDecisionRequest decision) {
        String reason = decision == null ? null : blankToNull(decision.reason());
        if (reason == null) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Cần nhập lý do từ chối");
        }
        String approverCode = currentEmployeeCode();
        LeaveRequest leaveRequest = findRequestForUpdate(id);
        requirePending(leaveRequest);
        leaveRequest.setStatus(REJECTED);
        leaveRequest.setApproverCode(approverCode);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setRejectReason(reason);
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional
    public List<LeaveBalanceResponse> getMyBalances(Long userId, int year) {
        String employeeCode = currentEmployeeCode();
        return leaveTypeRepository.findByIsDeletedFalseOrderByNameAsc().stream().map(leaveType -> {
            LeaveBalance balance = findOrCreateBalance(userId, employeeCode, leaveType, year);
            BigDecimal pending = pendingDays(employeeCode, leaveType.getId(), year, null);
            BigDecimal used = approvedDays(employeeCode, leaveType.getId(), year);
            BigDecimal remaining = balance.getTotalDays().subtract(used).subtract(pending).max(BigDecimal.ZERO);
            return new LeaveBalanceResponse(leaveType.getId(), leaveType.getName(), leaveType.getName(), year,
                    balance.getTotalDays(), used, pending, remaining);
        }).toList();
    }

    private String currentEmployeeCode() {
        try {
            ApiResponse<EmployeeProfileResponse> response = employeeClient.getCurrentEmployee();
            String employeeCode = response == null || response.getData() == null
                    ? null
                    : response.getData().employeeCode();
            if (employeeCode == null || employeeCode.isBlank()) {
                throw new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại");
            }
            return employeeCode.trim();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại");
        } catch (RestClientException exception) {
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "employee profile");
        }
    }

    private LeaveRequest findRequestForUpdate(Long id) {
        return leaveRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn nghỉ phép", id));
    }

    private LeaveType findActiveLeaveType(Long id) {
        return leaveTypeRepository.findById(id)
                .filter(type -> !Boolean.TRUE.equals(type.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Loại nghỉ phép", id));
    }

    private LeaveBalance findOrCreateBalance(Long userId, String employeeCode, LeaveType leaveType, int year) {
        BigDecimal annualQuota = BigDecimal.valueOf(leaveType.getMaxDaysPerYear());
        BigDecimal initialTotal = annualQuota.add(carryover(employeeCode, userId, leaveType, year));
        if (userId == null) {
            leaveBalanceRepository.createLegacyIfMissing(employeeCode, leaveType.getId(), year, initialTotal);
            return leaveBalanceRepository.findLegacyForUpdate(employeeCode, leaveType.getId(), year)
                    .orElseThrow(() -> new IllegalStateException("Không thể khởi tạo hạn mức nghỉ phép"));
        }
        leaveBalanceRepository.createIfMissing(employeeCode, userId, leaveType.getId(), year, initialTotal);
        return leaveBalanceRepository.findForUpdate(userId, leaveType.getId(), year)
                .orElseThrow(() -> new IllegalStateException("Không thể khởi tạo hạn mức nghỉ phép"));
    }

    private BigDecimal carryover(String employeeCode, Long userId, LeaveType leaveType, int year) {
        if (year <= 1 || leaveType.getCarryoverDays() == null || leaveType.getCarryoverDays() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal previousRemaining = (userId == null
                ? leaveBalanceRepository.findByRequesterUserIdIsNullAndEmployeeCodeAndTypeAndBalanceYear(
                        employeeCode, leaveType.getId(), year - 1)
                : leaveBalanceRepository.findByRequesterUserIdAndTypeAndBalanceYear(userId, leaveType.getId(), year - 1))
                .map(previous -> previous.getTotalDays().subtract(approvedDays(employeeCode, leaveType.getId(), year - 1)).max(BigDecimal.ZERO))
                .orElseGet(() -> BigDecimal.valueOf(leaveType.getMaxDaysPerYear())
                        .subtract(approvedDays(employeeCode, leaveType.getId(), year - 1)).max(BigDecimal.ZERO));
        return previousRemaining.min(BigDecimal.valueOf(leaveType.getCarryoverDays()));
    }

    private BigDecimal approvedDays(String employeeCode, Long leaveTypeId, int year) {
        return leaveRequestRepository.findByEmployeeCodeAndTypeAndStatusInAndStartDateBetween(
                        employeeCode, leaveTypeId, List.of(APPROVED), LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
                .stream().map(LeaveRequest::getTotalDays).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal pendingDays(String employeeCode, Long leaveTypeId, int year, Long excludedRequestId) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return leaveRequestRepository.findByEmployeeCodeAndTypeAndStatusInAndStartDateBetween(
                        employeeCode, leaveTypeId, List.of(PENDING), from, to).stream()
                .filter(request -> excludedRequestId == null || !excludedRequestId.equals(request.getId()))
                .map(LeaveRequest::getTotalDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        Collection<Holiday> holidays = holidayRepository
                .findByIsDeletedFalseAndHolidayDateLessThanEqualAndHolidayDateEndGreaterThanEqualOrderByHolidayDateAsc(endDate, startDate);
        Set<LocalDate> holidayDates = holidays.stream().flatMap(holiday ->
                        holiday.getHolidayDate().datesUntil(holiday.getHolidayDateEnd().plusDays(1)))
                .collect(java.util.stream.Collectors.toSet());
        long days = startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .filter(date -> !holidayDates.contains(date))
                .count();
        return BigDecimal.valueOf(days);
    }

    private void ensureAvailable(BigDecimal usedDays, BigDecimal pendingDays,
                                 BigDecimal requestedDays, BigDecimal totalDays) {
        BigDecimal committed = usedDays.add(pendingDays).add(requestedDays);
        if (committed.compareTo(totalDays) > 0) {
            throw new BusinessException(DomainCode.CONFLICT, "Số ngày nghỉ vượt quá hạn mức còn lại");
        }
    }

    private void requirePending(LeaveRequest leaveRequest) {
        if (!PENDING.equals(leaveRequest.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "Đơn không còn ở trạng thái chờ duyệt");
        }
    }

    private String normalizeStatus(String status) {
        String trimmed = status.trim();
        String normalizedCode = trimmed.toUpperCase(Locale.ROOT);
        if (LeaveRequestStatus.LABELS.containsKey(normalizedCode)) {
            return normalizedCode;
        }
        return LeaveRequestStatus.LABELS.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(trimmed))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BusinessException(DomainCode.INVALID_PARAMETER, "status"));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        if (startDate.getYear() != endDate.getYear()) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Một đơn nghỉ phép chỉ áp dụng trong cùng một năm");
        }
    }

    private void validateSearchRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Khoảng ngày tìm kiếm không hợp lệ");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LeaveRequestResponse toResponse(LeaveRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(request.getType()).orElse(null);
        return LeaveRequestResponse.from(request, leaveType);
    }
}
