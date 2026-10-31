package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.AuditLogs;
import com.example.employee_service.feature.entity.Users;
import com.example.employee_service.feature.model.response.AuditLogResponse;
import com.example.employee_service.feature.model.response.AuditLogSummaryResponse;
import com.example.employee_service.feature.repository.AuditLogsRepository;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter OCCURRED_AT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditLogsRepository auditLogsRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(String search, String module, String action, String result,
                                                  LocalDate from, LocalDate to, List<String> sorts,
                                                  int page, int size) {
        LocalDateTime fromInclusive = from == null ? null : from.atStartOfDay();
        LocalDateTime toExclusive = to == null ? null : to.plusDays(1).atStartOfDay();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        String normalizedSearch = limit(normalize(search), 100);
        normalizedSearch = normalizedSearch == null ? "" : normalizedSearch.toLowerCase(Locale.ROOT);
        String normalizedModule = normalize(module);
        String normalizedAction = normalize(action);
        String normalizedResult = normalize(result);

        Page<AuditLogs> logs = auditLogsRepository.search(
                normalizedSearch,
                normalizedModule,
                normalizedAction,
                normalizedResult,
                fromInclusive,
                toExclusive,
                PageRequest.of(safePage, safeSize, sort(sorts)));
        Map<Long, Users> users = usersRepository.findAllById(logs.getContent().stream()
                        .map(AuditLogs::getUser)
                        .filter(id -> id != null)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));

        return PageResponse.of(logs.map(log -> toResponse(log, users.get(log.getUser()))));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogSummaryResponse summary() {
        long successful = auditLogsRepository.countByResult("SUCCESS");
        long failed = auditLogsRepository.countByResult("FAILURE");
        return new AuditLogSummaryResponse(
                auditLogsRepository.count(),
                successful,
                failed,
                auditLogsRepository.countByEntityType("AUTH"));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String entityType, Long entityId,
                       String requestUri, String ipAddress, String userAgent, String result) {
        AuditLogs log = new AuditLogs();
        log.setUser(userId);
        log.setAction(limit(action, 50));
        log.setEntityType(limit(entityType, 50));
        log.setEntityId(entityId);
        log.setRequestUri(limit(requestUri, 500));
        log.setIpAddress(limit(ipAddress, 45));
        log.setUserAgent(limit(userAgent, 500));
        log.setResult("FAILURE".equals(result) ? "FAILURE" : "SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        auditLogsRepository.save(log);
    }

    private AuditLogResponse toResponse(AuditLogs log, Users user) {
        String actor = user == null ? "unknown" : user.getUserName();
        String actorName = user == null
                ? "Không xác định"
                : user.getFullName() == null || user.getFullName().isBlank()
                        ? user.getUserName()
                        : user.getFullName();
        String module = moduleLabel(log.getEntityType());
        String target = log.getEntityId() == null
                ? module
                : module + " · #" + log.getEntityId();
        return new AuditLogResponse(
                log.getId(),
                log.getCreatedAt() == null ? "—" : log.getCreatedAt().format(OCCURRED_AT_FORMAT),
                actor,
                actorName,
                actionLabel(log.getAction()),
                module,
                target,
                "FAILURE".equals(log.getResult()) ? "Thất bại" : "Thành công",
                log.getIpAddress(),
                log.getUserAgent(),
                log.getRequestUri());
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "CREATE" -> "Tạo mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "LOGIN" -> "Đăng nhập";
            case "LOGOUT" -> "Đăng xuất";
            case "CHANGE_PASSWORD" -> "Đổi mật khẩu";
            case "FORGOT_PASSWORD" -> "Khôi phục mật khẩu";
            case "TOKEN_REFRESH" -> "Làm mới phiên";
            case "CHECK_IN" -> "Chấm công vào";
            case "CHECK_OUT" -> "Chấm công ra";
            default -> action;
        };
    }

    private String moduleLabel(String entityType) {
        return switch (entityType) {
            case "AUTH" -> "Xác thực";
            case "EMPLOYEE" -> "Nhân viên";
            case "CONTRACT" -> "Hợp đồng";
            case "ORGANIZATION" -> "Tổ chức";
            case "POSITION" -> "Chức vụ";
            case "USER" -> "Tài khoản";
            case "ROLE" -> "Vai trò";
            case "ROLE_PERMISSION" -> "Phân quyền";
            case "PERMISSION" -> "Quyền";
            case "SHIFT" -> "Ca làm";
            case "SHIFT_ASSIGNMENT" -> "Phân ca";
            case "ATTENDANCE" -> "Chấm công";
            default -> entityType;
        };
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Sort sort(List<String> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }

        Map<String, String> fields = Map.of(
                "occurredAt", "createdAt",
                "action", "action",
                "module", "entityType",
                "target", "entityId",
                "result", "result",
                "ipAddress", "ipAddress");
        List<Sort.Order> orders = new ArrayList<>();
        for (String value : sorts) {
            if (value == null) continue;
            String[] parts = value.split(",", 2);
            String field = fields.get(parts[0]);
            if (field == null) continue;
            Sort.Direction direction = parts.length == 2 && "asc".equals(parts[1].toLowerCase(Locale.ROOT))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            orders.add(new Sort.Order(direction, field));
        }
        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }
        orders.add(Sort.Order.desc("id"));
        return Sort.by(orders);
    }

    private String limit(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
