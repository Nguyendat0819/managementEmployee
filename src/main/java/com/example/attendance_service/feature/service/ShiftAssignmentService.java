package com.example.attendance_service.feature.service;

import com.example.attendance_service.feature.model.request.ShiftAssignmentRequest;
import com.example.attendance_service.feature.model.response.ShiftAssignmentResponse;

import java.util.List;
import java.time.LocalDate;

public interface ShiftAssignmentService {
    List<ShiftAssignmentResponse> list();

    ShiftAssignmentResponse getCurrent(String employeeCode, LocalDate workDate);

    ShiftAssignmentResponse create(ShiftAssignmentRequest request);

    ShiftAssignmentResponse update(Long id, ShiftAssignmentRequest request);

    void delete(Long id);
}
