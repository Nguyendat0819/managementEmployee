package com.example.attendance_service.feature.repository;

import com.example.attendance_service.feature.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findAllByOrderByIdAsc();

    boolean existsByShiftNameIgnoreCaseAndIdNot(String shiftName, Long id);

    boolean existsByShiftNameIgnoreCase(String shiftName);
}
