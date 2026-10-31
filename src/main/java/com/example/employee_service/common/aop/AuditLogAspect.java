package com.example.employee_service.common.aop;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.feature.model.request.LoginRequest;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.AuditLogService;
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

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.employee_service.common.util.SecurityUtil.getJwt;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private static final Set<String> MUTATION_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Pattern NUMERIC_PATH_SEGMENT = Pattern.compile("(?:^|/)(\\d+)(?:/|$)");

    private final AuditLogService auditLogService;
    private final UsersRepository usersRepository;
    private final ObjectMapper objectMapper;

    @Around("within(com.example.employee_service.feature.controller..*)")
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
            record(request, path, response, joinPoint.getArgs(), "SUCCESS");
            return response;
        } catch (Throwable failure) {
            record(request, path, null, joinPoint.getArgs(), "FAILURE");
            throw failure;
        }
    }

    private boolean shouldAudit(String method, String path) {
        if (!path.startsWith("/api/") || path.startsWith("/api/audit-logs")) {
            return false;
        }
        if (path.equals("/api/auth/login") || path.equals("/api/auth/logout")
                || path.equals("/api/auth/refresh") || path.equals("/api/auth/forgot-password")) {
            return true;
        }
        return MUTATION_METHODS.contains(method) && !path.endsWith("/search") && !path.endsWith("/test");
    }

    private void record(HttpServletRequest request, String path, Object response, Object[] arguments, String result) {
        try {
            auditLogService.record(
                    currentUserId(request, arguments),
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

    private Long currentUserId(HttpServletRequest request, Object[] arguments) {
        Long authenticatedUserId = getJwt().map(jwt -> {
            try {
                return Long.valueOf(jwt.getSubject());
            } catch (RuntimeException exception) {
                return null;
            }
        }).orElse(null);
        if (authenticatedUserId != null || !requestPath(request).equals("/api/auth/login")) {
            return authenticatedUserId;
        }

        String username = Arrays.stream(arguments)
                .filter(LoginRequest.class::isInstance)
                .map(LoginRequest.class::cast)
                .map(LoginRequest::getUsername)
                .findFirst()
                .orElse(null);
        if (username == null || username.isBlank()) {
            return null;
        }
        try {
            var user = usersRepository.findByUserNameAndIsDeletedFalse(username);
            return user == null ? null : user.getId();
        } catch (RuntimeException failure) {
            log.warn("Could not resolve audit actor for login attempt");
            return null;
        }
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

        try {
            JsonNode id = objectMapper.valueToTree(apiResponse.getData()).path("id");
            return id.isIntegralNumber() ? id.longValue() : null;
        } catch (RuntimeException failure) {
            return null;
        }
    }

    private String action(String method, String path) {
        if (path.equals("/api/auth/login")) return "LOGIN";
        if (path.equals("/api/auth/logout")) return "LOGOUT";
        if (path.equals("/api/auth/refresh")) return "TOKEN_REFRESH";
        if (path.equals("/api/auth/forgot-password")) return "FORGOT_PASSWORD";
        if (path.equals("/api/auth/change-password")) return "CHANGE_PASSWORD";
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    private String module(String path) {
        if (path.startsWith("/api/auth")) return "AUTH";
        if (path.startsWith("/api/employee-contract")) return "CONTRACT";
        if (path.startsWith("/api/employee")) return "EMPLOYEE";
        if (path.startsWith("/api/departments")) return "ORGANIZATION";
        if (path.startsWith("/api/position")) return "POSITION";
        if (path.startsWith("/api/role-permission")) return "ROLE_PERMISSION";
        if (path.startsWith("/api/role")) return "ROLE";
        if (path.startsWith("/api/permission")) return "PERMISSION";
        if (path.startsWith("/api/user")) return "USER";
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
