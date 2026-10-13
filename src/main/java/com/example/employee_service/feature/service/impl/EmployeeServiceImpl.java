package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.Departments;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.entity.Positions;
import com.example.employee_service.feature.model.request.EmployeeRequest;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.repository.DepartmentsRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.service.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeesRepository employeesRepository;
    DepartmentsRepository departmentsRepository;
    PositionsRepository positionsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees(String search, String departmentCode, String positionCode, String status, String employmentType) {
        Map<String, String> departments = departmentsRepository.findAllByIsDeletedFalseOrderByLevelAscNameAsc().stream()
                .collect(Collectors.toMap(Departments::getDeptCode, Departments::getName, (first, ignored) -> first));
        Map<String, String> positions = positionsRepository.findAllByIsDeletedFalseOrderByTitleAsc().stream()
                .collect(Collectors.toMap(Positions::getPositionCode, Positions::getTitle, (first, ignored) -> first));
        return employeesRepository.search(normalizeSearch(search), normalize(departmentCode), normalize(positionCode), normalize(status), normalize(employmentType))
                .stream().map(employee -> toEmployeeResponse(employee, departments, positions)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(EmployeeSearchRequest request) {
        request.setSearch(normalize(request.getSearch()));
        request.setDepartmentCode(normalize(request.getDepartmentCode()));
        request.setPositionCode(normalize(request.getPositionCode()));
        request.setStatus(normalize(request.getStatus()));
        request.setEmploymentType(normalize(request.getEmploymentType()));
        return PageResponse.of(employeesRepository.search(request));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        return toEmployeeResponse(activeEmployee(id));
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        String code = request.getEmployeeCode().trim();
        // Mã cũ không được tái sử dụng vì các bảng nghiệp vụ lưu tham chiếu theo mã.
        if (employeesRepository.existsByEmployeeCodeIgnoreCase(code)) {
            throw new BusinessException(DomainCode.CONFLICT, "mã nhân viên " + code);
        }
        validateReferences(request);
        Employees employee = new Employees();
        applyRequest(employee, request);
        employee.setIsDeleted(false);
        return toEmployeeResponse(employeesRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employees employee = activeEmployee(id);
        String code = request.getEmployeeCode().trim();
        if (employeesRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException(DomainCode.CONFLICT, "mã nhân viên " + code);
        }
        validateReferences(request);
        applyRequest(employee, request);
        return toEmployeeResponse(employeesRepository.save(employee));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employees employee = activeEmployee(id);
        // Hồ sơ có thể đã được hợp đồng và bảng nghiệp vụ khác tham chiếu bằng mã nhân viên.
        employee.setIsDeleted(true);
        employeesRepository.save(employee);
    }

    private Employees activeEmployee(Long id) {
        return employeesRepository.findById(id)
                .filter(employee -> !Boolean.TRUE.equals(employee.getIsDeleted()))
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "nhân viên " + id));
    }

    private void validateReferences(EmployeeRequest request) {
        if (departmentsRepository.findByDeptCodeAndIsDeleted(request.getDepartmentCode().trim(), false) == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, "phòng ban " + request.getDepartmentCode());
        }
        if (!positionsRepository.existsByPositionCodeAndIsDeletedFalse(request.getPositionCode().trim())) {
            throw new BusinessException(DomainCode.NOT_FOUND, "vị trí " + request.getPositionCode());
        }
        if (request.getContractEnd() != null && request.getContractEnd().isBefore(request.getHireDate())) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "hạn hợp đồng phải sau ngày vào làm");
        }
    }

    private void applyRequest(Employees employee, EmployeeRequest request) {
        employee.setEmployeeCode(request.getEmployeeCode().trim());
        employee.setFullName(request.getFullName().trim());
        employee.setBirthDate(request.getBirthDate());
        employee.setGender(normalize(request.getGender()));
        employee.setIdCard(normalize(request.getIdCard()));
        employee.setPhone(normalize(request.getPhone()));
        employee.setEmail(normalize(request.getEmail()));
        employee.setDepartmentCode(request.getDepartmentCode().trim());
        employee.setPositionCode(request.getPositionCode().trim());
        employee.setEmploymentType(request.getEmploymentType().trim());
        employee.setHireDate(request.getHireDate());
        employee.setContractEnd(request.getContractEnd());
        employee.setStatus(request.getStatus().trim());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeSearch(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private EmployeeResponse toEmployeeResponse(Employees employee) {
        Departments department = employee.getDepartmentCode() == null ? null
                : departmentsRepository.findByDeptCodeAndIsDeleted(employee.getDepartmentCode(), false);
        String positionName = employee.getPositionCode() == null ? null : positionsRepository
                .findByPositionCodeAndIsDeletedFalse(employee.getPositionCode())
                .map(Positions::getTitle).orElse(null);
        return toEmployeeResponse(employee,
                department == null ? Map.of() : Map.of(department.getDeptCode(), department.getName()),
                positionName == null ? Map.of() : Map.of(employee.getPositionCode(), positionName));
    }

    private EmployeeResponse toEmployeeResponse(Employees employee, Map<String, String> departments, Map<String, String> positions) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .user(employee.getUserId())
                .fullName(employee.getFullName())
                .birthDate(employee.getBirthDate())
                .gender(employee.getGender())
                .idCard(employee.getIdCard())
                .phone(employee.getPhone())
                .email(employee.getEmail())
                .departmentCode(employee.getDepartmentCode())
                .departmentName(departments.get(employee.getDepartmentCode()))
                .positionCode(employee.getPositionCode())
                .positionName(positions.get(employee.getPositionCode()))
                .employmentType(employee.getEmploymentType())
                .hireDate(employee.getHireDate())
                .contractEnd(employee.getContractEnd())
                .status(employee.getStatus())
                .isDeleted(employee.getIsDeleted())
                .build();
    }
}
