package com.example.leave_service.feature.service.impl;

import com.example.leave_service.common.exception.BusinessException;
import com.example.leave_service.common.exception.ResourceNotFoundException;
import com.example.leave_service.common.response.DomainCode;
import com.example.leave_service.feature.entity.Holiday;
import com.example.leave_service.feature.model.request.SaveHolidayRequest;
import com.example.leave_service.feature.model.response.HolidayResponse;
import com.example.leave_service.feature.repository.LeaveHolidayRepository;
import com.example.leave_service.feature.service.LeaveHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveHolidayServiceImpl implements LeaveHolidayService {

    private final LeaveHolidayRepository holidayRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> list(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Khoảng ngày tìm kiếm không hợp lệ");
        }
        List<Holiday> holidays = from == null || to == null
                ? holidayRepository.findByIsDeletedFalseOrderByHolidayDateAsc()
                : holidayRepository.findByIsDeletedFalseAndHolidayDateLessThanEqualAndHolidayDateEndGreaterThanEqualOrderByHolidayDateAsc(to, from);
        return holidays.stream().map(HolidayResponse::from).toList();
    }

    @Override
    @Transactional
    public HolidayResponse save(Long id, SaveHolidayRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
        Holiday holiday = id == null
                ? new Holiday()
                : holidayRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ngày nghỉ", id));
        holiday.setHolidayDate(request.startDate());
        holiday.setHolidayDateEnd(request.endDate());
        holiday.setName(request.name().trim());
        holiday.setHolidayType(request.type().trim());
        holiday.setNotes(request.notes() == null ? null : request.notes().trim());
        holiday.setIsDeleted(false);
        return HolidayResponse.from(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ngày nghỉ", id));
        holiday.setIsDeleted(true);
        holidayRepository.save(holiday);
    }
}
