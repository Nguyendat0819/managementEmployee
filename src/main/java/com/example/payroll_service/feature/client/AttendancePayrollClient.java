package com.example.payroll_service.feature.client;

import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.feature.model.request.PayrollAttendanceSummaryRequest;
import com.example.payroll_service.feature.model.response.PayrollAttendanceSummaryResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange
public interface AttendancePayrollClient {
    @PostExchange("/attendance-service/api/attendance/payroll-summary")
    ApiResponse<List<PayrollAttendanceSummaryResponse>> payrollSummary(
            @RequestBody PayrollAttendanceSummaryRequest request);
}
