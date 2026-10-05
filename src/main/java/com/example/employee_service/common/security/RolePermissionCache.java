package com.example.employee_service.common.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.security.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RolePermissionCache implements PermissionResolver {

    /** Khoảng cách tối thiểu giữa 2 lần nạp lazy — chặn gọi Admin dồn dập. */
    static long LAZY_MIN_INTERVAL_MS = 10_000L;

    private final RolePermissionProperties properties;
    private final AtomicReference<Map<String, Set<String>>> ref = new AtomicReference<>(Map.of());

    /** Thời điểm nạp gần nhất — dùng để throttle nạp lazy. */
    private final AtomicLong lastReloadAt = new AtomicLong(0);

    public RolePermissionCache(RolePermissionProperties properties) {
        this.properties = properties;
    }

    @Override
    public Set<String> permissionsOf(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
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
        Map<String, Set<String>> newCache = new HashMap<>();
        properties.getRolePermissions().forEach((role, permissions) ->
                newCache.put(role, permissions == null ? Set.of() : Set.copyOf(permissions)));
        ref.set(Map.copyOf(newCache));
        lastReloadAt.set(System.currentTimeMillis());
        log.info("RolePermissionCache reloaded from configuration: {} roles", newCache.size());
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
