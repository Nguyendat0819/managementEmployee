package com.example.attendance_service.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

public record ShiftRequest(
        @NotBlank @Size(max = 100) String shiftName,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotEmpty List<@Pattern(regexp = "T[2-7]|CN") String> dayOfWeek,
        @NotNull Boolean active
) {
}
