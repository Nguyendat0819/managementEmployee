package com.example.leave_service.feature.repository;

import com.example.leave_service.common.persistence.BaseRepository;
import com.example.leave_service.feature.entity.Holiday;

import java.time.LocalDate;
import java.util.List;

public interface LeaveHolidayRepository extends BaseRepository<Holiday, Long> {

    List<Holiday> findByIsDeletedFalseAndHolidayDateLessThanEqualAndHolidayDateEndGreaterThanEqualOrderByHolidayDateAsc(
            LocalDate endDate, LocalDate startDate);

    List<Holiday> findByIsDeletedFalseOrderByHolidayDateAsc();
}
