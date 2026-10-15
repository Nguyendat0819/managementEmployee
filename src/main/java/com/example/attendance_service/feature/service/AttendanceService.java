package com.example.attendance_service.feature.service;

import com.example.attendance_service.feature.model.request.CheckInRequest;
import com.example.attendance_service.feature.model.request.CheckOutRequest;
import com.example.attendance_service.feature.model.response.AttendanceResponse;
import com.example.attendance_service.common.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse checkIn(CheckInRequest request);

    AttendanceResponse checkOut(CheckOutRequest request);

    List<AttendanceResponse> history(String employeeCode, LocalDate from, LocalDate to);

    PageResponse<AttendanceResponse> search(LocalDate from, LocalDate to, String employeeCode,
                                            String status, int page, int size);
}
