package com.example.leave_service.feature.model.response;

import com.example.leave_service.feature.entity.Holiday;

import java.time.LocalDate;

public record HolidayResponse(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        String name,
        String type,
        String notes
) {
    public static HolidayResponse from(Holiday holiday) {
        return new HolidayResponse(holiday.getId(), holiday.getHolidayDate(), holiday.getHolidayDateEnd(),
                holiday.getName(), holiday.getHolidayType(), holiday.getNotes());
    }
}
