package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.feature.entity.Positions;
import com.example.employee_service.feature.model.request.PositionCreateRequest;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PositionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionServiceImplTest {
    @Mock(mockMaker = "mock-maker-subclass") PositionsRepository positionsRepository;
    @Mock(mockMaker = "mock-maker-subclass") EmployeesRepository employeesRepository;
    @InjectMocks PositionServiceImpl service;

    @Test
    void createNormalizesCodeBeforeSaving() {
        PositionCreateRequest request = request(" senior_specialist ", BigDecimal.TEN, BigDecimal.valueOf(20));
        when(positionsRepository.save(any(Positions.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createPosition(request);

        assertEquals("SENIOR_SPECIALIST", response.getPositionCode());
        verify(positionsRepository).existsByPositionCodeIgnoreCase("SENIOR_SPECIALIST");
        verify(positionsRepository).save(any(Positions.class));
    }

    @Test
    void createRejectsInvalidSalaryRange() {
        PositionCreateRequest request = request("DIRECTOR", BigDecimal.valueOf(20), BigDecimal.TEN);

        assertThrows(BusinessException.class, () -> service.createPosition(request));

        verify(positionsRepository, never()).save(any());
    }

    @Test
    void deleteRejectsPositionUsedByActiveEmployee() {
        Positions position = activePosition("DIRECTOR");
        when(positionsRepository.findById(1L)).thenReturn(Optional.of(position));
        when(employeesRepository.existsByPositionCodeAndIsDeletedFalse("DIRECTOR")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.deletePosition(1L));

        verify(positionsRepository, never()).save(any());
    }

    @Test
    void deleteSoftDeletesUnusedPosition() {
        Positions position = activePosition("DIRECTOR");
        when(positionsRepository.findById(1L)).thenReturn(Optional.of(position));

        service.deletePosition(1L);

        assertTrue(position.getIsDeleted());
        verify(positionsRepository).save(position);
    }

    private PositionCreateRequest request(String code, BigDecimal min, BigDecimal max) {
        PositionCreateRequest request = new PositionCreateRequest();
        request.setPositionCode(code);
        request.setTitle("Giám đốc");
        request.setLevel("DIRECTOR");
        request.setBaseSalaryMin(min);
        request.setBaseSalaryMax(max);
        return request;
    }

    private Positions activePosition(String code) {
        Positions position = new Positions();
        position.setPositionCode(code);
        position.setTitle("Giám đốc");
        position.setLevel("DIRECTOR");
        position.setIsDeleted(false);
        return position;
    }
}
