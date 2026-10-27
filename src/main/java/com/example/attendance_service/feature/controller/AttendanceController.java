package com.example.attendance_service.feature.controller;

import com.example.attendance_service.common.response.ApiResponse;
import com.example.attendance_service.common.response.ApiResponseFactory;
import com.example.attendance_service.common.response.PageResponse;
import com.example.attendance_service.feature.controller.api.AttendanceApi;
import com.example.attendance_service.feature.model.request.CheckInRequest;
import com.example.attendance_service.feature.model.request.CheckOutRequest;
import com.example.attendance_service.feature.model.request.PayrollAttendanceRequest;
import com.example.attendance_service.feature.model.response.AttendanceResponse;
import com.example.attendance_service.feature.model.response.PayrollAttendanceSummaryResponse;
import com.example.attendance_service.feature.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttendanceController implements AttendanceApi {

    private final AttendanceService attendanceService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<AttendanceResponse> checkIn(@Valid CheckInRequest request) {
        return responseFactory.success(attendanceService.checkIn(request));
    }

    @Override
    public ApiResponse<AttendanceResponse> checkOut(@Valid CheckOutRequest request) {
        return responseFactory.success(attendanceService.checkOut(request));
    }

    @Override
    public ApiResponse<List<AttendanceResponse>> history(String employeeCode, LocalDate from, LocalDate to) {
        return responseFactory.success(attendanceService.history(employeeCode, from, to));
    }

    @Override
    public ApiResponse<PageResponse<AttendanceResponse>> search(LocalDate from, LocalDate to,
                                                                 String employeeCode, String status,
                                                                 int page, int size) {
        return responseFactory.success(attendanceService.search(from, to, employeeCode, status, page, size));
    }

    @Override
    public ApiResponse<List<PayrollAttendanceSummaryResponse>> payrollSummary(PayrollAttendanceRequest request) {
        return responseFactory.success(attendanceService.payrollSummary(request));
    }
}
