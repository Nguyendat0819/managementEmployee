package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.model.request.EmployeeRequest;
import com.example.employee_service.feature.repository.DepartmentsRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PositionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @Mock(mockMaker = "mock-maker-subclass") EmployeesRepository employeesRepository;
    @Mock(mockMaker = "mock-maker-subclass") DepartmentsRepository departmentsRepository;
    @Mock(mockMaker = "mock-maker-subclass") PositionsRepository positionsRepository;
    @InjectMocks EmployeeServiceImpl service;

    @Test
    void createRejectsMissingDepartmentBeforeSaving() {
        EmployeeRequest request = request();
        assertThrows(BusinessException.class, () -> service.createEmployee(request));
        verify(employeesRepository, never()).save(any());
    }

    @Test
    void deleteKeepsEmployeeRecordForHistoricalReferences() {
        Employees employee = new Employees();
        employee.setIsDeleted(false);
        when(employeesRepository.findById(7L)).thenReturn(Optional.of(employee));

        service.deleteEmployee(7L);

        verify(employeesRepository).save(employee);
        org.junit.jupiter.api.Assertions.assertTrue(employee.getIsDeleted());
        verify(employeesRepository, never()).delete(any());
    }

    @Test
    void createRejectsReusedCodeEvenWhenOldRecordWasSoftDeleted() {
        EmployeeRequest request = request();
        when(employeesRepository.existsByEmployeeCodeIgnoreCase("NV-01")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.createEmployee(request));

        verify(departmentsRepository, never()).findByDeptCodeAndIsDeleted(any(), any());
        verify(employeesRepository, never()).save(any());
    }

    private EmployeeRequest request() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmployeeCode("NV-01");
        request.setFullName("Nguyễn An");
        request.setDepartmentCode("IT");
        request.setPositionCode("DEV");
        request.setEmploymentType("Toàn thời gian");
        request.setHireDate(LocalDate.of(2026, 1, 1));
        request.setStatus("Đang làm việc");
        return request;
    }
}
