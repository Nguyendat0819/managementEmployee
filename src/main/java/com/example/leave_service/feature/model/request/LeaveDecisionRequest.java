package com.example.leave_service.feature.model.request;

import jakarta.validation.constraints.Size;

public record LeaveDecisionRequest(@Size(max = 2000) String reason) {
}
