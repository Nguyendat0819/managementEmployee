package com.example.leave_service.feature.service.impl;

import com.example.leave_service.common.exception.BusinessException;
import com.example.leave_service.common.exception.ResourceNotFoundException;
import com.example.leave_service.common.response.DomainCode;
import com.example.leave_service.feature.entity.LeaveType;
import com.example.leave_service.feature.model.request.SaveLeaveTypeRequest;
import com.example.leave_service.feature.model.response.LeaveTypeResponse;
import com.example.leave_service.feature.repository.LeaveTypeRepository;
import com.example.leave_service.feature.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> list(boolean includeInactive) {
        List<LeaveType> leaveTypes = includeInactive
                ? leaveTypeRepository.findAll()
                : leaveTypeRepository.findByIsDeletedFalseOrderByNameAsc();
        return leaveTypes.stream().map(LeaveTypeResponse::from).toList();
    }

    @Override
    @Transactional
    public LeaveTypeResponse save(Long id, SaveLeaveTypeRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        boolean duplicate = id == null
                ? leaveTypeRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(code)
                : leaveTypeRepository.existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(code, id);
        if (duplicate) {
            throw new BusinessException(DomainCode.CONFLICT, "Mã loại nghỉ phép đã tồn tại");
        }

        LeaveType leaveType = id == null
                ? new LeaveType()
                : leaveTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loại nghỉ phép", id));
        leaveType.setCode(code);
        leaveType.setName(request.name().trim());
        leaveType.setMaxDaysPerYear(request.annualQuota());
        leaveType.setCarryoverDays(request.carryoverDays());
        leaveType.setPaidLeave(request.paidLeave());
        leaveType.setRequireDocument(request.requireDocument());
        leaveType.setIsDeleted(!request.active());
        leaveType.setDescription(request.description() == null ? null : request.description().trim());
        return LeaveTypeResponse.from(leaveTypeRepository.save(leaveType));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại nghỉ phép", id));
        leaveType.setIsDeleted(true);
        leaveTypeRepository.save(leaveType);
    }
}
