package com.example.attendance_service.feature.service;

import com.example.attendance_service.feature.model.request.ShiftRequest;
import com.example.attendance_service.feature.model.response.ShiftResponse;

import java.util.List;

public interface ShiftService {
    List<ShiftResponse> list();

    ShiftResponse create(ShiftRequest request);

    ShiftResponse update(Long id, ShiftRequest request);

    void delete(Long id);
}
