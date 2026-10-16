package com.example.payroll_service.feature.service;

import com.example.payroll_service.feature.model.request.PayrollRunRequest;
import com.example.payroll_service.feature.model.request.PayslipLineRequest;
import com.example.payroll_service.feature.model.response.PayrollRunResponse;
import com.example.payroll_service.feature.model.response.PayslipResponse;

import java.util.List;

public interface PayrollRunService {
    List<PayrollRunResponse> getAll();

    PayrollRunResponse getById(Long id);

    PayrollRunResponse create(PayrollRunRequest request);

    void deleteDraft(Long id);

    List<PayslipResponse> addPayslips(Long runId, List<PayslipLineRequest> entries);

    List<PayslipResponse> getPayslips(Long runId);

    PayrollRunResponse submitForApproval(Long id);

    PayrollRunResponse approve(Long id);

    PayrollRunResponse markPaid(Long id);
}
