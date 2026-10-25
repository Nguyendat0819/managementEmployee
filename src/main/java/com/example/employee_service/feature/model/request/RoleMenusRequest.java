package com.example.employee_service.feature.model.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoleMenusRequest(@NotNull List<@NotNull String> menuCodes) {
}
