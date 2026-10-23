package com.example.leave_service.feature.service;

import com.example.leave_service.feature.model.request.SaveLeaveTypeRequest;
import com.example.leave_service.feature.model.response.LeaveTypeResponse;

import java.util.List;

public interface LeaveTypeService {

    List<LeaveTypeResponse> list(boolean includeInactive);

    LeaveTypeResponse save(Long id, SaveLeaveTypeRequest request);

    void delete(Long id);
}
