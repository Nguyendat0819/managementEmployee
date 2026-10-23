package com.example.leave_service.feature.model.response;

import com.example.leave_service.feature.entity.LeaveRequest;
import com.example.leave_service.feature.entity.LeaveType;
import com.example.leave_service.feature.model.LeaveRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestResponse(
        Long id,
        String requestCode,
        Long employeeUserId,
        String employeeCode,
        String employeeName,
        String department,
        String position,
        Long leaveTypeId,
        String type,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal days,
        String reason,
        String evidenceUrl,
        String statusCode,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime processedAt,
        String approver,
        String decisionNote
) {
    public static LeaveRequestResponse from(LeaveRequest request, LeaveType leaveType) {
        String employeeCode = request.getEmployeeCode();
        String statusCode = request.getStatus();
        return new LeaveRequestResponse(
                request.getId(),
                request.getRequestCode(),
                request.getRequesterUserId(),
                employeeCode,
                employeeCode,
                null,
                null,
                request.getType(),
                leaveType == null ? null : leaveType.getName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalDays(),
                request.getReason(),
                request.getEvidenceUrl(),
                statusCode,
                LeaveRequestStatus.LABELS.getOrDefault(statusCode, statusCode),
                request.getCreatedAt(),
                request.getApprovedAt(),
                request.getApproverCode(),
                request.getRejectReason()
        );
    }
}
