package com.example.leave_service.feature.model;

import java.util.Map;

public final class LeaveRequestStatus {

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";
    public static final Map<String, String> LABELS = Map.of(
            PENDING, "Chờ duyệt",
            APPROVED, "Đã duyệt",
            REJECTED, "Từ chối",
            CANCELLED, "Đã hủy"
    );

    private LeaveRequestStatus() {
    }
}
