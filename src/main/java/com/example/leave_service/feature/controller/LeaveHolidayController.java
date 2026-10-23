package com.example.leave_service.feature.controller;

import com.example.leave_service.common.response.ApiResponse;
import com.example.leave_service.common.response.ApiResponseFactory;
import com.example.leave_service.feature.controller.api.LeaveHolidayApi;
import com.example.leave_service.feature.model.request.SaveHolidayRequest;
import com.example.leave_service.feature.model.response.HolidayResponse;
import com.example.leave_service.feature.service.LeaveHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaveHolidayController implements LeaveHolidayApi {

    private final LeaveHolidayService leaveHolidayService;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<HolidayResponse>> list(LocalDate from, LocalDate to) {
        return responseFactory.success(leaveHolidayService.list(from, to));
    }

    @Override
    public ApiResponse<HolidayResponse> create(SaveHolidayRequest request) {
        return responseFactory.success(leaveHolidayService.save(null, request));
    }

    @Override
    public ApiResponse<HolidayResponse> update(Long id, SaveHolidayRequest request) {
        return responseFactory.success(leaveHolidayService.save(id, request));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        leaveHolidayService.delete(id);
        return responseFactory.success();
    }
}
