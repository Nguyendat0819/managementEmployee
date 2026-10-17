package com.example.payroll_service.feature.repository;

import com.example.payroll_service.common.persistence.BaseRepository;
import com.example.payroll_service.feature.entity.PayrollHoliday;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollHolidayRepository extends BaseRepository<PayrollHoliday, Long> {
    List<PayrollHoliday> findAllByOrderByHolidayDateAsc();

    List<PayrollHoliday> findAllByHolidayDateLessThanEqualAndHolidayDateEndGreaterThanEqual(
            LocalDate end, LocalDate start);
}
