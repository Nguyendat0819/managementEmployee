package com.example.employee_service.common.security;

import com.example.employee_service.common.persistence.dto.RolePermissionDto;
import com.example.employee_service.feature.service.RolePermissionService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(name = "app.security.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RolePermissionCache implements PermissionResolver {

    /** Dành cho cơ chế nạp lazy; hiện tại reload chạy lúc khởi tạo và theo lịch. */
    static long LAZY_MIN_INTERVAL_MS = 10_000L;

    private final RolePermissionProperties properties;
    private final AtomicReference<Map<String, Set<String>>> ref = new AtomicReference<>(Map.of());

    /** Lưu thời điểm reload gần nhất để cơ chế nạp lazy có thể throttle nếu được kích hoạt. */
    private final AtomicLong lastReloadAt = new AtomicLong(0);

    RolePermissionService rolePermissionService;
    @Override
    public Set<String> permissionsOf(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        // Converter truyền role đã đọc từ JWT vào đây; mỗi request chỉ tra cache,
        // không đọc lại bảng role-permission trong database.
        Map<String, Set<String>> map = ref.get();
        Set<String> result = new HashSet<>();
        for (String role : roles) {
            result.addAll(map.getOrDefault(role, Set.of()));
        }
        return result;
    }

    @PostConstruct
    @Scheduled(fixedRateString = "${app.admin.refresh-ms:300000}",
            initialDelayString = "${app.admin.refresh-ms:300000}")
    public void reload() {
        try {
            List<RolePermissionDto> allRolePermissions = rolePermissionService.getAllPermissions();
            if (allRolePermissions.isEmpty()) {
                log.warn("RolePermissionCache reload skipped because no active role-permission was found");
                return;
            }

            Map<String, Set<String>> newCache = new HashMap<>();
            for (RolePermissionDto dto : allRolePermissions) {
                newCache.computeIfAbsent(dto.getRole(), key -> new HashSet<>())
                        .addAll(dto.getPermissions());
            }

            // Swap nguyên snapshot sau khi build xong để request đang chạy không đọc cache dở dang.
            ref.set(Map.copyOf(newCache));
            lastReloadAt.set(System.currentTimeMillis());
            log.info("RolePermissionCache reloaded from database: {} roles", newCache.keySet());
        } catch (RuntimeException exception) {
            // Lỗi nguồn dữ liệu không được làm mất quyền đang hoạt động trong cache hiện tại.
            log.warn("RolePermissionCache reload failed; keeping the previous snapshot", exception);
        }
    }

    /** Truy cập nội bộ cho test / debug. */
    public Map<String, Set<String>> currentCache() {
        return ref.get();
    }

    /** Tái nạp cache với map truyền vào (dùng cho test). */
    void reset(Map<String, Set<String>> newMap) {
        ref.set(newMap == null ? Map.of() : newMap);
        lastReloadAt.set(System.currentTimeMillis());
    }
}
