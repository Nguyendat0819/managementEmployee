package com.example.leave_service.feature.model.response;

import java.math.BigDecimal;

public record LeaveBalanceResponse(
        Long leaveTypeId,
        String type,
        String label,
        Integer year,
        BigDecimal totalDays,
        BigDecimal usedDays,
        BigDecimal pendingDays,
        BigDecimal remainingDays
) {
}
