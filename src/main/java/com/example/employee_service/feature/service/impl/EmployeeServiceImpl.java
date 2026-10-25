package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.entity.Departments;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.entity.Positions;
import com.example.employee_service.feature.entity.Users;
import com.example.employee_service.feature.model.request.EmployeeRequest;
import com.example.employee_service.feature.model.request.EmployeeSearchRequest;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.model.response.EmployeeCatalogResponse;
import com.example.employee_service.feature.model.response.EmployeeUserOptionResponse;
import com.example.employee_service.feature.repository.DepartmentsRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.repository.PositionsRepository;
import com.example.employee_service.feature.repository.UsersRepository;
import com.example.employee_service.feature.service.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.employee_service.common.util.Const.IS_ACTIVE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeesRepository employeesRepository;
    DepartmentsRepository departmentsRepository;
    PositionsRepository positionsRepository;
    UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getCurrentEmployee(Long userId, String email) {
        return toEmployeeResponse(currentEmployee(userId, email));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeCatalogResponse getCatalogs() {
        return new EmployeeCatalogResponse(
                List.of("Nam", "Nữ", "Khác"),
                List.of("Toàn thời gian", "Bán thời gian", "Thử việc"),
                List.of("Đang làm việc", "Đang thử việc", "Tạm nghỉ")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeUserOptionResponse> getUnlinkedUsers() {
        Set<Long> linkedUserIds = Set.copyOf(employeesRepository.findLinkedUserIds());
        return usersRepository.findAllByIsDeletedFalseAndStatusOrderByUserNameAsc(IS_ACTIVE).stream()
                .filter(user -> !linkedUserIds.contains(user.getId()))
                .map(user -> new EmployeeUserOptionResponse(user.getId(), user.getUserName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees(String search, String departmentCode, String positionCode, String status, String employmentType) {
        Map<String, String> departments = departmentsRepository.findAllByIsDeletedFalseOrderByLevelAscNameAsc().stream()
                .collect(Collectors.toMap(Departments::getDeptCode, Departments::getName, (first, ignored) -> first));
        Map<String, Positions> positions = positionsRepository.findAllByIsDeletedFalseOrderByTitleAsc().stream()
                .collect(Collectors.toMap(Positions::getPositionCode, position -> position, (first, ignored) -> first));
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
        linkLoginAccount(employee, request.getUserId());
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
        linkLoginAccount(employee, request.getUserId());
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

    private Employees currentEmployee(Long userId, String email) {
        Employees employee = employeesRepository.findByUserIdAndIsDeletedFalse(userId).orElse(null);
        if (employee != null) {
            return employee;
        }
        if (email != null && !email.isBlank()) {
            // Email fallback only covers legacy unlinked rows; it must not cross an existing user_id link.
            List<Employees> matches = employeesRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(email.trim())
                    .stream()
                    .filter(candidate -> candidate.getUserId() == null || candidate.getUserId().equals(userId))
                    .toList();
            if (matches.size() == 1) {
                return matches.get(0);
            }
            if (matches.size() > 1) {
                throw new BusinessException(DomainCode.CONFLICT, "email được dùng bởi nhiều hồ sơ nhân viên");
            }
        }
        throw new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại");
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

    private void linkLoginAccount(Employees employee, Long requestedUserId) {
        if (requestedUserId != null) {
            Users account = usersRepository.findById(requestedUserId)
                    .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()) && IS_ACTIVE.equals(user.getStatus()))
                    .orElseThrow(() -> new BusinessException(DomainCode.NOT_FOUND, "tài khoản " + requestedUserId));
            employeesRepository.findByUserIdAndIsDeletedFalse(requestedUserId)
                    .filter(linkedEmployee -> !linkedEmployee.getId().equals(employee.getId()))
                    .ifPresent(linkedEmployee -> {
                        throw new BusinessException(DomainCode.CONFLICT, "tài khoản đã được liên kết với hồ sơ nhân viên khác");
                    });
            employee.setUserId(account.getId());
            return;
        }
        if (employee.getUserId() != null || employee.getEmail() == null || employee.getEmail().isBlank()) {
            return;
        }

        List<Users> accounts = usersRepository.findAllByEmailIgnoreCaseAndIsDeletedFalse(employee.getEmail().trim());
        if (accounts.size() > 1) {
            throw new BusinessException(DomainCode.CONFLICT, "email được dùng bởi nhiều tài khoản");
        }
        if (accounts.isEmpty()) {
            return;
        }

        Users account = accounts.get(0);
        if (employeesRepository.findByUserIdAndIsDeletedFalse(account.getId()).isPresent()) {
            throw new BusinessException(DomainCode.CONFLICT, "tài khoản đã được liên kết với hồ sơ nhân viên khác");
        }
        employee.setUserId(account.getId());
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
        Positions position = employee.getPositionCode() == null ? null : positionsRepository
                .findByPositionCodeAndIsDeletedFalse(employee.getPositionCode())
                .orElse(null);
        return toEmployeeResponse(employee,
                department == null ? Map.of() : Map.of(department.getDeptCode(), department.getName()),
                position == null ? Map.of() : Map.of(employee.getPositionCode(), position));
    }

    private EmployeeResponse toEmployeeResponse(Employees employee, Map<String, String> departments, Map<String, Positions> positions) {
        Positions position = positions.get(employee.getPositionCode());
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
                .positionName(position == null ? null : position.getTitle())
                .positionSalaryMin(position == null ? null : position.getBaseSalaryMin())
                .positionSalaryMax(position == null ? null : position.getBaseSalaryMax())
                .employmentType(employee.getEmploymentType())
                .hireDate(employee.getHireDate())
                .contractEnd(employee.getContractEnd())
                .status(employee.getStatus())
                .isDeleted(employee.getIsDeleted())
                .build();
    }
}
