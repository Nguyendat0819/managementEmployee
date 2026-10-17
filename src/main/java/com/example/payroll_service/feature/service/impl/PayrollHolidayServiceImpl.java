package com.example.payroll_service.feature.service.impl;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.exception.ResourceNotFoundException;
import com.example.payroll_service.common.response.DomainCode;
import com.example.payroll_service.feature.entity.PayrollHoliday;
import com.example.payroll_service.feature.model.request.PayrollHolidayRequest;
import com.example.payroll_service.feature.model.response.PayrollHolidayResponse;
import com.example.payroll_service.feature.repository.PayrollHolidayRepository;
import com.example.payroll_service.feature.service.PayrollHolidayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayrollHolidayServiceImpl implements PayrollHolidayService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    PayrollHolidayRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollHolidayResponse> getAll() {
        return repository.findAllByOrderByHolidayDateAsc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PayrollHolidayResponse save(Long id, PayrollHolidayRequest request) {
        if (request.holidayDateEnd().isBefore(request.holidayDate())) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "holidayDate/holidayDateEnd");
        }
        List<PayrollHoliday> overlaps = repository.findAllByHolidayDateLessThanEqualAndHolidayDateEndGreaterThanEqual(
                request.holidayDateEnd(), request.holidayDate());
        if (overlaps.stream().anyMatch(existing -> !existing.getId().equals(id))) {
            throw new BusinessException(DomainCode.CONFLICT, "ngày nghỉ lễ bị trùng lịch");
        }

        PayrollHoliday holiday = id == null ? new PayrollHoliday() : repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ngày nghỉ lễ " + id));
        holiday.setName(request.name().trim());
        holiday.setHolidayType(request.holidayType().trim());
        holiday.setHolidayDate(request.holidayDate());
        holiday.setHolidayDateEnd(request.holidayDateEnd());
        holiday.setNotes(request.notes() == null ? "" : request.notes().trim());
        return toResponse(repository.save(holiday));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PayrollHoliday holiday = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ngày nghỉ lễ " + id));
        repository.delete(holiday);
    }

    private PayrollHolidayResponse toResponse(PayrollHoliday holiday) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        String status = today.isBefore(holiday.getHolidayDate()) ? "Sắp tới"
                : today.isAfter(holiday.getHolidayDateEnd()) ? "Đã qua" : "Đang diễn ra";
        int dayCount = (int) ChronoUnit.DAYS.between(holiday.getHolidayDate(), holiday.getHolidayDateEnd()) + 1;
        return new PayrollHolidayResponse(holiday.getId(), holiday.getName(), holiday.getHolidayType(),
                holiday.getHolidayDate(), holiday.getHolidayDateEnd(), dayCount, holiday.getNotes(), status);
    }
}
