package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.util.SecurityUtil;
import com.example.employee_service.feature.model.request.MenuRequest;
import com.example.employee_service.feature.model.request.RoleMenusRequest;
import com.example.employee_service.feature.model.response.MenuResponse;
import com.example.employee_service.feature.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final ApiResponseFactory responses;

    @GetMapping("/me")
    public ApiResponse<List<MenuResponse>> currentMenu() {
        return responses.success(menuService.forRoles(SecurityUtil.getRoles()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<MenuResponse>> list() {
        return responses.success(menuService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MenuResponse> create(@Valid @RequestBody MenuRequest request) {
        return responses.success(menuService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MenuResponse> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        return responses.success(menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return responses.success();
    }

    @GetMapping("/roles/{roleCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<String>> roleMenus(@PathVariable String roleCode) {
        return responses.success(menuService.assignedToRole(roleCode));
    }

    @PutMapping("/roles/{roleCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateRoleMenus(@PathVariable String roleCode, @Valid @RequestBody RoleMenusRequest request) {
        menuService.replaceRoleMenus(roleCode, request.menuCodes());
        return responses.success();
    }
}
