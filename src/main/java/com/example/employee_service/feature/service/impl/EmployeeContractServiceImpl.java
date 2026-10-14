package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.Departments;
import com.example.employee_service.feature.entity.EmployeeContracts;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.entity.Positions;
import com.example.employee_service.feature.model.request.EmployeeContractCreateRequest;
import com.example.employee_service.feature.model.request.EmployeeContractSearchRequest;
import com.example.employee_service.feature.model.request.EmployeeContractUpdateRequest;
import com.example.employee_service.feature.model.response.EmployeeContractResponse;
import com.example.employee_service.feature.repository.DepartmentsRepository;
import com.example.employee_service.feature.repository.EmployeeContractsRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.service.EmployeeContractService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeContractServiceImpl implements EmployeeContractService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    EmployeeContractsRepository contractsRepository;
    EmployeesRepository employeesRepository;
    DepartmentsRepository departmentsRepository;
    PositionsRepository positionsRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeContractResponse> searchContracts(EmployeeContractSearchRequest request) {
        request.setSearch(normalize(request.getSearch()));
        request.setContractType(normalize(request.getContractType()));
        request.setEmployeeCode(normalize(request.getEmployeeCode()));
        request.setEmploymentType(normalize(request.getEmploymentType()));
        request.setStatus(normalize(request.getStatus()));
        if (request.getIsDeleted() == null) {
            request.setIsDeleted(false);
        }
        return PageResponse.of(contractsRepository.search(request));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeContractResponse getContract(Long id) {
        return toResponse(activeContract(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeContractResponse getCurrentContract(Long userId, String email) {
        Employees employee = employeesRepository.findByUserIdAndIsDeletedFalse(userId).orElse(null);
        if (employee == null && email != null && !email.isBlank()) {
            List<Employees> matches = employeesRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(email.trim())
                    .stream()
                    .filter(candidate -> candidate.getUserId() == null || candidate.getUserId().equals(userId))
                    .toList();
            if (matches.size() > 1) {
                throw new BusinessException(DomainCode.CONFLICT, "email được dùng bởi nhiều hồ sơ nhân viên");
            }
            employee = matches.stream().findFirst()
                    .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại"));
        }
        if (employee == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại");
        }
        LocalDate today = LocalDate.now();
        return contractsRepository
                .findAllByEmployeeCodeIgnoreCaseAndIsDeletedFalseOrderByStartDateDescIdDesc(employee.getEmployeeCode())
                .stream()
                .filter(contract -> !contract.getStartDate().isAfter(today))
                .filter(contract -> contract.getEndDate() == null || !contract.getEndDate().isBefore(today))
                .findFirst()
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public EmployeeContractResponse createContract(EmployeeContractCreateRequest request) {
        String contractNo = request.getContractNo().trim();
        if (contractsRepository.existsByContractNoIgnoreCase(contractNo)) {
            throw new BusinessException(DomainCode.CONFLICT, "số hợp đồng " + contractNo);
        }

        EmployeeContracts contract = new EmployeeContracts();
        applyRequest(contract, request);
        contract.setIsDeleted(false);
        return toResponse(contractsRepository.save(contract));
    }

    @Override
    @Transactional
    public EmployeeContractResponse updateContract(Long id, EmployeeContractUpdateRequest request) {
        EmployeeContracts contract = activeContract(id);
        String contractNo = request.getContractNo().trim();
        if (contractsRepository.existsByContractNoIgnoreCaseAndIdNot(contractNo, id)) {
            throw new BusinessException(DomainCode.CONFLICT, "số hợp đồng " + contractNo);
        }

        applyRequest(contract, request);
        return toResponse(contractsRepository.save(contract));
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        EmployeeContracts contract = activeContract(id);
        contract.setIsDeleted(true);
        contractsRepository.save(contract);
    }

    private void applyRequest(EmployeeContracts contract, EmployeeContractCreateRequest request) {
        applyCommonFields(contract, request.getContractNo(), request.getContractType(), request.getEmployeeCode(),
                request.getActualSalary(), request.getEmploymentType(), request.getStartDate(), request.getEndDate(),
                request.getSignedDate(), request.getStatus());
    }

    private void applyRequest(EmployeeContracts contract, EmployeeContractUpdateRequest request) {
        applyCommonFields(contract, request.getContractNo(), request.getContractType(), request.getEmployeeCode(),
                request.getActualSalary(), request.getEmploymentType(), request.getStartDate(), request.getEndDate(),
                request.getSignedDate(), request.getStatus());
    }

    private void applyCommonFields(EmployeeContracts contract, String contractNo, String contractType,
                                   String employeeCode, java.math.BigDecimal actualSalary,
                                   String employmentType, LocalDate startDate, LocalDate endDate,
                                   LocalDate signedDate, String status) {
        Employees employee = employeesRepository.findByEmployeeCodeIgnoreCaseAndIsDeletedFalse(employeeCode.trim())
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "nhân viên " + employeeCode));
        validateDates(employee, startDate, endDate);

        Departments department = departmentsRepository.findByDeptCodeAndIsDeleted(employee.getDepartmentCode(), false);
        Positions position = positionsRepository.findByPositionCodeAndIsDeletedFalse(employee.getPositionCode()).orElse(null);

        contract.setContractNo(contractNo.trim());
        contract.setContractType(contractType.trim());
        contract.setEmployeeCode(employee.getEmployeeCode());
        contract.setFullName(employee.getFullName());
        contract.setBirthDate(employee.getBirthDate());
        contract.setIdCard(employee.getIdCard());
        contract.setDepartmentCode(employee.getDepartmentCode());
        contract.setDepartmentName(department == null ? null : department.getName());
        contract.setPositionCode(employee.getPositionCode());
        contract.setPositionName(position == null ? null : position.getTitle());
        contract.setActualSalary(actualSalary);
        contract.setEmploymentType(employmentType.trim());
        contract.setHireDate(employee.getHireDate());
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setSignedDate(signedDate);
        contract.setStatus(status.trim());
    }

    private void validateDates(Employees employee, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(employee.getHireDate())) {
            String hireDate = employee.getHireDate().format(DATE_FORMATTER);
            throw new BusinessException(DomainCode.VALIDATION_ERROR,
                    String.format("nhân viên %s có ngày vào làm %s, ngày bắt đầu hợp đồng không được trước ngày này",
                            employee.getFullName(), hireDate));
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(DomainCode.VALIDATION_ERROR, "ngày kết thúc hợp đồng phải sau ngày bắt đầu");
        }
    }

    private EmployeeContracts activeContract(Long id) {
        return contractsRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "hợp đồng " + id));
    }

    private EmployeeContractResponse toResponse(EmployeeContracts contract) {
        return EmployeeContractResponse.builder()
                .id(contract.getId())
                .contractNo(contract.getContractNo())
                .contractType(contract.getContractType())
                .employeeCode(contract.getEmployeeCode())
                .fullName(contract.getFullName())
                .birthDate(contract.getBirthDate())
                .idCard(contract.getIdCard())
                .departmentCode(contract.getDepartmentCode())
                .departmentName(contract.getDepartmentName())
                .positionCode(contract.getPositionCode())
                .positionName(contract.getPositionName())
                .actualSalary(contract.getActualSalary())
                .employmentType(contract.getEmploymentType())
                .hireDate(contract.getHireDate())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .signedDate(contract.getSignedDate())
                .status(contract.getStatus())
                .isDeleted(contract.getIsDeleted())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
