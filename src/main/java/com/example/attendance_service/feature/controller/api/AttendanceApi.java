package com.example.attendance_service.feature.controller.api;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.feature.model.request.CheckInRequest;
import com.example.attendance_service.feature.model.request.CheckOutRequest;
import com.example.attendance_service.feature.model.response.AttendanceResponse;
import com.example.attendance_service.common.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/attendance")
public interface AttendanceApi {
    @PostMapping("/check-in")
    ApiResponse<AttendanceResponse> checkIn(@Valid @RequestBody CheckInRequest request);

    @PostMapping("/check-out")
    ApiResponse<AttendanceResponse> checkOut(@Valid @RequestBody CheckOutRequest request);

    @GetMapping("/history")
    ApiResponse<List<AttendanceResponse>> history(
            @RequestParam String employeeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);

    @GetMapping("/records")
    ApiResponse<PageResponse<AttendanceResponse>> search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);
}
