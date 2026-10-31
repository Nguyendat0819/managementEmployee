package com.example.attendance_service.common.aop;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.feature.service.AuditLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.attendance_service.common.util.SecurityUtil.getJwt;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private static final Set<String> MUTATION_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Pattern NUMERIC_PATH_SEGMENT = Pattern.compile("(?:^|/)(\\d+)(?:/|$)");

    private final AuditLogRecorder auditLogRecorder;
    private final ObjectMapper objectMapper;

    @Around("within(com.example.attendance_service.feature.controller..*)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String path = requestPath(request);
        if (!shouldAudit(request.getMethod(), path)) {
            return joinPoint.proceed();
        }

        try {
            Object response = joinPoint.proceed();
            record(request, path, response, "SUCCESS");
            return response;
        } catch (Throwable failure) {
            record(request, path, null, "FAILURE");
            throw failure;
        }
    }

    private boolean shouldAudit(String method, String path) {
        if (!path.startsWith("/api/") || path.startsWith("/api/audit-logs")) {
            return false;
        }
        if ("GET".equals(method) && path.equals("/api/attendance/records")) {
            return true;
        }
        if (path.endsWith("/payroll-summary") || path.endsWith("/search")) {
            return false;
        }
        return MUTATION_METHODS.contains(method);
    }

    private void record(HttpServletRequest request, String path, Object response, String result) {
        try {
            auditLogRecorder.record(
                    currentUserId(),
                    action(request.getMethod(), path),
                    module(path),
                    entityId(path, response),
                    request.getMethod() + " " + path,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    result);
        } catch (RuntimeException failure) {
            log.error("Could not persist audit event for {} {}", request.getMethod(), path, failure);
        }
    }

    private Long currentUserId() {
        return getJwt().map(jwt -> {
            try {
                return Long.valueOf(jwt.getSubject());
            } catch (RuntimeException exception) {
                return null;
            }
        }).orElse(null);
    }

    private Long entityId(String path, Object response) {
        Matcher matcher = NUMERIC_PATH_SEGMENT.matcher(path);
        Long pathId = null;
        while (matcher.find()) {
            pathId = Long.valueOf(matcher.group(1));
        }
        if (pathId != null || !(response instanceof ApiResponse<?> apiResponse) || apiResponse.getData() == null) {
            return pathId;
        }

        JsonNode id = objectMapper.valueToTree(apiResponse.getData()).path("id");
        return id.isIntegralNumber() ? id.longValue() : null;
    }

    private String action(String method, String path) {
        if (path.equals("/api/attendance/check-in")) return "CHECK_IN";
        if (path.equals("/api/attendance/check-out")) return "CHECK_OUT";
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            case "GET" -> "READ";
            default -> method;
        };
    }

    private String module(String path) {
        if (path.startsWith("/api/shifts")) return "SHIFT";
        if (path.startsWith("/api/shift-assignments")) return "SHIFT_ASSIGNMENT";
        if (path.startsWith("/api/attendance")) return "ATTENDANCE";
        return "SYSTEM";
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;
    }
}
