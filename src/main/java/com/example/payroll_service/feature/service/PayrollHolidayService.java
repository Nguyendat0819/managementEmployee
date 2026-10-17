package com.example.payroll_service.feature.service;

import com.example.payroll_service.feature.model.request.PayrollHolidayRequest;
import com.example.payroll_service.feature.model.response.PayrollHolidayResponse;

import java.util.List;

public interface PayrollHolidayService {
    List<PayrollHolidayResponse> getAll();

    PayrollHolidayResponse save(Long id, PayrollHolidayRequest request);

    void delete(Long id);
}
