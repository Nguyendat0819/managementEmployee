package com.example.leave_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLeaveRequestRequest(
        @NotNull Long leaveTypeId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank @Size(max = 2000) String reason,
        @Size(max = 2000) String evidenceUrl
) {
}
