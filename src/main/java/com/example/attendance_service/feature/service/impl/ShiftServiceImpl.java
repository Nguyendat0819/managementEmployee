package com.example.attendance_service.feature.service.impl;

import com.example.attendance_service.common.exception.BusinessException;
import com.example.attendance_service.common.exception.ResourceNotFoundException;
import com.example.attendance_service.common.response.DomainCode;
import com.example.attendance_service.feature.entity.Shift;
import com.example.attendance_service.feature.model.request.ShiftRequest;
import com.example.attendance_service.feature.model.response.ShiftResponse;
import com.example.attendance_service.feature.repository.EmployeeShiftAssignmentRepository;
import com.example.attendance_service.feature.repository.ShiftRepository;
import com.example.attendance_service.feature.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> list() {
        return shiftRepository.findAllByOrderByIdAsc().stream()
                .map(shift -> ShiftResponse.from(shift, assignmentRepository.countByShift(shift.getId())))
                .toList();
    }

    @Override
    @Transactional
    public ShiftResponse create(ShiftRequest request) {
        validate(request);
        if (shiftRepository.existsByShiftNameIgnoreCase(request.shiftName().trim())) {
            throw new BusinessException(DomainCode.CONFLICT, "Tên ca đã tồn tại");
        }
        Shift shift = new Shift();
        apply(shift, request);
        return ShiftResponse.from(shiftRepository.save(shift), 0);
    }

    @Override
    @Transactional
    public ShiftResponse update(Long id, ShiftRequest request) {
        validate(request);
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ca làm việc " + id));
        if (shiftRepository.existsByShiftNameIgnoreCaseAndIdNot(request.shiftName().trim(), id)) {
            throw new BusinessException(DomainCode.CONFLICT, "Tên ca đã tồn tại");
        }
        apply(shift, request);
        return ShiftResponse.from(shiftRepository.save(shift), assignmentRepository.countByShift(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ca làm việc " + id));
        shift.setIsDeleted(true);
        shiftRepository.save(shift);
    }

    private void validate(ShiftRequest request) {
        if (request.startTime().equals(request.endTime())) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "startTime/endTime");
        }
    }

    private void apply(Shift shift, ShiftRequest request) {
        shift.setShiftName(request.shiftName().trim());
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setDayOfWeek(request.dayOfWeek().stream().distinct().toList());
        shift.setIsDeleted(!request.active());
    }
}
