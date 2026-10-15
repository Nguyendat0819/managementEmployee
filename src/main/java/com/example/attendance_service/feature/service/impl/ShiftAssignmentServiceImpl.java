package com.example.attendance_service.feature.service.impl;

import com.example.attendance_service.common.exception.BusinessException;
import com.example.attendance_service.common.exception.ResourceNotFoundException;
import com.example.attendance_service.common.response.DomainCode;
import com.example.attendance_service.feature.entity.EmployeeShiftAssignment;
import com.example.attendance_service.feature.entity.Shift;
import com.example.attendance_service.feature.model.request.ShiftAssignmentRequest;
import com.example.attendance_service.feature.model.response.ShiftAssignmentResponse;
import com.example.attendance_service.feature.repository.EmployeeShiftAssignmentRepository;
import com.example.attendance_service.feature.repository.ShiftRepository;
import com.example.attendance_service.feature.service.ShiftAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final ShiftRepository shiftRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignmentResponse> list() {
        return assignmentRepository.findAllByOrderByEffectiveDateDesc().stream()
                .map(assignment -> ShiftAssignmentResponse.from(assignment,
                        shiftRepository.findById(assignment.getShift()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftAssignmentResponse getCurrent(String employeeCode, LocalDate workDate) {
        if (employeeCode == null || employeeCode.isBlank() || workDate == null) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "employeeCode/date");
        }
        return assignmentRepository.findActiveAssignments(employeeCode.trim(), workDate).stream()
                .findFirst()
                .map(assignment -> {
                    Shift shift = shiftRepository.findById(assignment.getShift())
                            .filter(value -> !Boolean.TRUE.equals(value.getIsDeleted()))
                            .orElse(null);
                    return shift == null ? null : ShiftAssignmentResponse.from(assignment, shift);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public ShiftAssignmentResponse create(ShiftAssignmentRequest request) {
        validate(request, null);
        EmployeeShiftAssignment assignment = new EmployeeShiftAssignment();
        apply(assignment, request);
        EmployeeShiftAssignment saved = assignmentRepository.save(assignment);
        return ShiftAssignmentResponse.from(saved, activeShift(request.shiftId()));
    }

    @Override
    @Transactional
    public ShiftAssignmentResponse update(Long id, ShiftAssignmentRequest request) {
        validate(request, id);
        EmployeeShiftAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("phân ca " + id));
        apply(assignment, request);
        EmployeeShiftAssignment saved = assignmentRepository.save(assignment);
        return ShiftAssignmentResponse.from(saved, activeShift(request.shiftId()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("phân ca " + id);
        }
        assignmentRepository.deleteById(id);
    }

    private void validate(ShiftAssignmentRequest request, Long assignmentId) {
        if (request.endDate() != null && request.endDate().isBefore(request.effectiveDate())) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "effectiveDate/endDate");
        }
        activeShift(request.shiftId());
        String employeeCode = request.employeeCode().trim();
        if (assignmentRepository.existsOverlappingAssignmentForEmployee(employeeCode, assignmentId,
                request.effectiveDate(), request.endDate())) {
            throw new BusinessException(DomainCode.CONFLICT, "Nhân viên đã có phân ca trong khoảng thời gian này");
        }
    }

    private Shift activeShift(Long shiftId) {
        return shiftRepository.findById(shiftId)
                .filter(shift -> !Boolean.TRUE.equals(shift.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("ca làm việc đang hoạt động " + shiftId));
    }

    private void apply(EmployeeShiftAssignment assignment, ShiftAssignmentRequest request) {
        assignment.setEmployeeCode(request.employeeCode().trim());
        assignment.setShift(request.shiftId());
        assignment.setEffectiveDate(request.effectiveDate());
        assignment.setEndDate(request.endDate());
    }
}
