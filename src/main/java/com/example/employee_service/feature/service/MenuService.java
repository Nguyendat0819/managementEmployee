package com.example.employee_service.feature.service;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.feature.model.request.MenuRequest;
import com.example.employee_service.feature.model.response.MenuResponse;
import com.example.employee_service.feature.repository.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final JdbcTemplate jdbc;
    private final RolesRepository rolesRepository;

    private static final String SELECT_ACTIVE = """
            SELECT id, menu_code, label, icon, icon_color, path, parent_code, sort_order
            FROM hr_service.menus WHERE is_deleted = false
            ORDER BY sort_order, id
            """;

    @Transactional(readOnly = true)
    public List<MenuResponse> list() {
        return jdbc.query(SELECT_ACTIVE, (rs, rowNum) -> new MenuResponse(
                rs.getLong("id"), rs.getString("menu_code"), rs.getString("label"),
                rs.getString("icon"), rs.getString("icon_color"), rs.getString("path"),
                rs.getString("parent_code"), rs.getInt("sort_order")));
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> forRoles(Set<String> roles) {
        if (roles.isEmpty()) return List.of();
        List<MenuResponse> all = list();
        Map<String, MenuResponse> byCode = new HashMap<>();
        all.forEach(menu -> byCode.put(menu.menuCode(), menu));
        Set<String> assigned = new HashSet<>();
        Set<String> visible = new HashSet<>();
        for (String role : roles) {
            String code = role.toUpperCase(Locale.ROOT);
            jdbc.query("SELECT menu_code FROM hr_service.role_menus WHERE upper(role_code) = ?", rs -> {
                String menuCode = rs.getString(1);
                assigned.add(menuCode);
                MenuResponse current = byCode.get(menuCode);
                while (current != null && visible.add(current.menuCode())) {
                    current = byCode.get(current.parentCode());
                }
            }, code);
        }
        return all.stream()
                .filter(menu -> visible.contains(menu.menuCode()))
                .map(menu -> menu.path() != null && !assigned.contains(menu.menuCode())
                        ? new MenuResponse(menu.id(), menu.menuCode(), menu.label(), menu.icon(), menu.iconColor(),
                                null, menu.parentCode(), menu.sortOrder())
                        : menu)
                .toList();
    }

    @Transactional
    public MenuResponse create(MenuRequest request) {
        String code = request.menuCode().trim();
        if (jdbc.queryForObject("SELECT count(*) FROM hr_service.menus WHERE menu_code = ?", Integer.class, code) > 0) {
            throw new BusinessException(DomainCode.CONFLICT, "mã menu " + code);
        }
        validate(request, null);
        jdbc.update("""
                INSERT INTO hr_service.menus (menu_code, label, icon, icon_color, path, parent_code, sort_order, is_deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, false, now(), now())
                """, code, request.label().trim(), request.icon().trim(), blankToNull(request.iconColor()),
                blankToNull(request.path()), blankToNull(request.parentCode()), request.sortOrder());
        return byCode(code);
    }

    @Transactional
    public MenuResponse update(Long id, MenuRequest request) {
        MenuResponse existing = byId(id);
        if (!existing.menuCode().equals(request.menuCode().trim())) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "không thể đổi mã menu");
        }
        validate(request, existing);
        jdbc.update("""
                UPDATE hr_service.menus SET label = ?, icon = ?, icon_color = ?, path = ?, parent_code = ?,
                    sort_order = ?, updated_at = now() WHERE id = ?
                """, request.label().trim(), request.icon().trim(), blankToNull(request.iconColor()),
                blankToNull(request.path()), blankToNull(request.parentCode()), request.sortOrder(), id);
        if (existing.path() != null && blankToNull(request.path()) == null) {
            jdbc.update("DELETE FROM hr_service.role_menus WHERE menu_code = ?", existing.menuCode());
        }
        return byId(id);
    }

    @Transactional
    public void delete(Long id) {
        MenuResponse menu = byId(id);
        if (jdbc.queryForObject("SELECT count(*) FROM hr_service.menus WHERE parent_code = ? AND is_deleted = false",
                Integer.class, menu.menuCode()) > 0) {
            throw new BusinessException(DomainCode.CONFLICT, "menu còn mục con");
        }
        jdbc.update("UPDATE hr_service.menus SET is_deleted = true, updated_at = now() WHERE id = ?", id);
        jdbc.update("DELETE FROM hr_service.role_menus WHERE menu_code = ?", menu.menuCode());
    }

    @Transactional(readOnly = true)
    public List<String> assignedToRole(String roleCode) {
        String code = activeRoleCode(roleCode);
        return jdbc.queryForList("SELECT menu_code FROM hr_service.role_menus WHERE role_code = ? ORDER BY menu_code",
                String.class, code);
    }

    @Transactional
    public void replaceRoleMenus(String roleCode, List<String> menuCodes) {
        String code = activeRoleCode(roleCode);
        Set<String> unique = new HashSet<>(menuCodes);
        Map<String, MenuResponse> available = new HashMap<>();
        list().forEach(menu -> available.put(menu.menuCode(), menu));
        for (String menuCode : unique) {
            MenuResponse menu = available.get(menuCode);
            if (menu == null || menu.path() == null) {
                throw new BusinessException(DomainCode.INVALID_PARAMETER, "menu " + menuCode);
            }
        }
        jdbc.update("DELETE FROM hr_service.role_menus WHERE role_code = ?", code);
        for (String menuCode : unique) {
            jdbc.update("INSERT INTO hr_service.role_menus (role_code, menu_code) VALUES (?, ?)", code, menuCode);
        }
    }

    private void validate(MenuRequest request, MenuResponse existing) {
        String parent = blankToNull(request.parentCode());
        if (parent != null) {
            byCode(parent);
            if (parent.equals(request.menuCode())) {
                throw new BusinessException(DomainCode.INVALID_PARAMETER, "menu không thể là cha của chính nó");
            }
            if (existing != null) {
                String ancestor = parent;
                while (ancestor != null) {
                    if (ancestor.equals(existing.menuCode())) {
                        throw new BusinessException(DomainCode.INVALID_PARAMETER, "cấu trúc menu bị lặp vòng");
                    }
                    ancestor = byCode(ancestor).parentCode();
                }
            }
        }
    }

    private String activeRoleCode(String roleCode) {
        return rolesRepository.findByRoleCodeIgnoreCaseAndIsDeletedFalse(roleCode)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "vai trò " + roleCode))
                .getRoleCode();
    }

    private MenuResponse byCode(String code) {
        return list().stream().filter(menu -> menu.menuCode().equals(code)).findFirst()
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "menu " + code));
    }

    private MenuResponse byId(Long id) {
        return list().stream().filter(menu -> menu.id().equals(id)).findFirst()
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "menu " + id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
