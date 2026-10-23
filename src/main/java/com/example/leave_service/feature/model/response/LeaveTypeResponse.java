package com.example.leave_service.feature.model.response;

import com.example.leave_service.feature.entity.LeaveType;

public record LeaveTypeResponse(
        Long id,
        String code,
        String name,
        Integer annualQuota,
        Integer carryoverDays,
        Boolean paidLeave,
        Boolean requireDocument,
        Boolean active,
        String status,
        String description
) {
    public static LeaveTypeResponse from(LeaveType leaveType) {
        boolean active = !Boolean.TRUE.equals(leaveType.getIsDeleted());
        return new LeaveTypeResponse(
                leaveType.getId(), leaveType.getCode(), leaveType.getName(), leaveType.getMaxDaysPerYear(),
                leaveType.getCarryoverDays(), leaveType.getPaidLeave(), leaveType.getRequireDocument(),
                active, active ? "Đang áp dụng" : "Tạm khóa", leaveType.getDescription()
        );
    }
}
