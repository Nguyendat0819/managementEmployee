package com.example.leave_service.feature.service;

import com.example.leave_service.feature.model.request.SaveHolidayRequest;
import com.example.leave_service.feature.model.response.HolidayResponse;

import java.time.LocalDate;
import java.util.List;

public interface LeaveHolidayService {

    List<HolidayResponse> list(LocalDate from, LocalDate to);

    HolidayResponse save(Long id, SaveHolidayRequest request);

    void delete(Long id);
}
