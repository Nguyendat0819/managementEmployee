package com.example.leave_service.feature.service;

import com.example.leave_service.common.response.PageResponse;
import com.example.leave_service.feature.model.request.CreateLeaveRequestRequest;
import com.example.leave_service.feature.model.request.LeaveDecisionRequest;
import com.example.leave_service.feature.model.response.LeaveBalanceResponse;
import com.example.leave_service.feature.model.response.LeaveRequestResponse;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestService {

    LeaveRequestResponse create(CreateLeaveRequestRequest request, Long userId);

    List<LeaveRequestResponse> getMine(Long userId);

    LeaveRequestResponse cancel(Long id, Long userId);

    PageResponse<LeaveRequestResponse> search(String status, String employeeCode,
                                               LocalDate from, LocalDate to, int page, int size);

    LeaveRequestResponse approve(Long id, LeaveDecisionRequest request);

    LeaveRequestResponse reject(Long id, LeaveDecisionRequest request);

    List<LeaveBalanceResponse> getMyBalances(Long userId, int year);
}
