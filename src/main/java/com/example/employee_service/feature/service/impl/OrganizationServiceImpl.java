package com.example.employee_service.feature.service.impl;

import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.util.Const;
import com.example.employee_service.feature.entity.Departments;
import com.example.employee_service.feature.entity.Employees;
import com.example.employee_service.feature.mapper.DepartMapper;
import com.example.employee_service.feature.model.request.DepartmentsRequest;
import com.example.employee_service.feature.model.request.DepartmentsUpdateRequest;
import com.example.employee_service.feature.model.response.DepartmentResponse;
import com.example.employee_service.feature.model.response.EmployeeResponse;
import com.example.employee_service.feature.repository.DepartmentsRepository;
import com.example.employee_service.feature.repository.EmployeesRepository;
import com.example.employee_service.feature.service.OrganizationService;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationServiceImpl implements OrganizationService {
    EmployeesRepository employeesRepository;
    DepartmentsRepository departmentsRepository;
    DepartMapper  departMapper;
    EntityManager entityManager;
    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentsRequest request) {
//      Kiểm tra hàm đã xóa hay chưa
        Departments departments = departmentsRepository.findByDeptCodeAndIsDeleted(request.getDeptCode(), Const.IN_DELETED);
        if(departments != null) {
            throw new BusinessException(DomainCode.CONFLICT, departments.getName());
        }
        String headCode = normalize(request.getHeadCode());
        if (headCode != null && employeesRepository.findByEmployeeCodeIgnoreCaseAndIsDeletedFalse(headCode).isEmpty()) {
            throw new BusinessException(DomainCode.NOT_FOUND, "nhân viên phụ trách " + headCode);
        }
        Departments newDepartments = departMapper.toEntity(request);
        newDepartments.setHeadCode(headCode);
        newDepartments.setIsDeleted(Const.IN_DELETED);
        Departments savedDepartment = departmentsRepository.saveAndFlush(newDepartments);
        entityManager.refresh(savedDepartment);
        return toDepartmentResponse(
                savedDepartment,
                countDepartmentEmployees(savedDepartment, employeesRepository.findAllByIsDeletedFalseOrderByFullNameAsc())
        );
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(String deptCode, DepartmentsUpdateRequest request){
//        Kiểm tra department
        Departments departments = departmentsRepository.findByDeptCodeAndIsDeleted(deptCode, Const.IN_DELETED);
        if(departments == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, deptCode);
        }
        String parentCode = normalize(request.getParentCode());
        String headCode = normalize(request.getHeadCode());
        Departments parent = parentCode == null ? null : departmentsRepository.findByDeptCodeAndIsDeleted(parentCode, false);
        if (parentCode != null && parent == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, "đơn vị cha " + parentCode);
        }
        validateParentChange(departments, parent);
        if (headCode != null && employeesRepository.findByEmployeeCodeIgnoreCaseAndIsDeletedFalse(headCode).isEmpty()) {
            throw new BusinessException(DomainCode.NOT_FOUND, "nhân viên phụ trách " + headCode);
        }

        departments.setName(request.getName().trim());
        departments.setParentCode(parentCode);
        departments.setHeadCode(headCode);
        departments.setLevel(parent == null ? (short) 1 : (short) (parent.getLevel() + 1));
        List<Departments> changedDepartments = updateDescendantLevels(departments);
        Departments updateDepartments = departmentsRepository.save(departments);
        departmentsRepository.saveAll(changedDepartments);
        departmentsRepository.flush();
        entityManager.refresh(updateDepartments);
        return toDepartmentResponse(
                updateDepartments,
                countDepartmentEmployees(updateDepartments, employeesRepository.findAllByIsDeletedFalseOrderByFullNameAsc())
        );
    }

    private void validateParentChange(Departments department, Departments parent) {
        Set<String> visited = new HashSet<>();
        Departments current = parent;
        while (current != null && visited.add(current.getDeptCode())) {
            if (current.getDeptCode().equals(department.getDeptCode())) {
                throw new BusinessException(DomainCode.VALIDATION_ERROR, "đơn vị cha không thể là chính nó hoặc đơn vị con");
            }
            current = current.getParentCode() == null
                    ? null
                    : departmentsRepository.findByDeptCodeAndIsDeleted(current.getParentCode(), false);
        }
    }

    private List<Departments> updateDescendantLevels(Departments department) {
        List<Departments> activeDepartments = departmentsRepository.findAllByIsDeletedFalseOrderByLevelAscNameAsc();
        List<Departments> changed = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        visited.add(department.getDeptCode());
        List<Departments> parents = List.of(department);
        while (!parents.isEmpty()) {
            List<Departments> children = new ArrayList<>();
            for (Departments parent : parents) {
                for (Departments candidate : activeDepartments) {
                    if (!parent.getDeptCode().equals(candidate.getParentCode()) || !visited.add(candidate.getDeptCode())) continue;
                    candidate.setLevel((short) (parent.getLevel() + 1));
                    changed.add(candidate);
                    children.add(candidate);
                }
            }
            parents = children;
        }
        return changed;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public void deleteDepartment(String deptCode) {
        Departments department = departmentsRepository.findByDeptCodeAndIsDeleted(deptCode, Const.IN_DELETED);
        if (department == null) {
            throw new BusinessException(DomainCode.NOT_FOUND, deptCode);
        }
        if (departmentsRepository.existsByParentCodeAndIsDeletedFalse(deptCode)) {
            throw new BusinessException(DomainCode.CONFLICT, "phòng ban còn đơn vị con");
        }
        if (employeesRepository.existsByDepartmentCodeAndIsDeletedFalse(deptCode)) {
            throw new BusinessException(DomainCode.CONFLICT, "phòng ban còn nhân sự");
        }

        // Giữ lại bản ghi để không phá vỡ lịch sử tham chiếu, nhưng loại khỏi các luồng đọc mặc định.
        department.setIsDeleted(Const.IS_DELETED);
        departmentsRepository.save(department);
    }

    @Override
    public List<DepartmentResponse> getDepartments(
            String search,
            Short level,
            String parentCode,
            Boolean isDeleted
    ) {
        List<Employees> employees = employeesRepository.findAllByIsDeletedFalseOrderByFullNameAsc();

        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String normalizedParentCode = parentCode == null ? null : parentCode.trim();
        Boolean deletedFilter = isDeleted == null ? Const.IN_DELETED : isDeleted;
        List<Departments> departments = departmentsRepository.findByFilters(
                normalizedSearch,
                level,
                normalizedParentCode,
                deletedFilter
        );

        return departments
                .stream()
                .map(department -> toDepartmentResponse(department, countDepartmentEmployees(department, employees)))
                .toList();
    }

    private long countDepartmentEmployees(Departments department, List<Employees> activeEmployees) {
        Set<String> employeeCodes = new HashSet<>();
        String headCode = normalizeEmployeeCode(department.getHeadCode());

        for (Employees employee : activeEmployees) {
            String employeeCode = normalizeEmployeeCode(employee.getEmployeeCode());
            if (employeeCode == null) continue;

            if (department.getDeptCode().equals(employee.getDepartmentCode()) || employeeCode.equals(headCode)) {
                employeeCodes.add(employeeCode);
            }
        }

        return employeeCodes.size();
    }

    private String normalizeEmployeeCode(String employeeCode) {
        return employeeCode == null || employeeCode.isBlank()
                ? null
                : employeeCode.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(String departmentCode) {
        List<Employees> departmentEmployees = new ArrayList<>(
                employeesRepository.findAllByIsDeletedFalseAndDepartmentCodeOrderByFullNameAsc(departmentCode)
        );
        Departments department = departmentsRepository.findByDeptCodeAndIsDeleted(departmentCode, Const.IN_DELETED);
        if (department != null && department.getHeadCode() != null) {
            employeesRepository.findByEmployeeCodeIgnoreCaseAndIsDeletedFalse(department.getHeadCode())
                    .ifPresent(head -> {
                        boolean alreadyIncluded = departmentEmployees.stream()
                                .anyMatch(employee -> employee.getEmployeeCode().equalsIgnoreCase(head.getEmployeeCode()));
                        if (!alreadyIncluded) departmentEmployees.add(head);
                    });
        }

        return departmentEmployees.stream()
                .sorted(Comparator.comparing(Employees::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toEmployeeResponse)
                .toList();
    }

    private DepartmentResponse toDepartmentResponse(Departments department, long employeeCount) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .deptCode(department.getDeptCode())
                .parentCode(department.getParentCode())
                .name(department.getName())
                .code(department.getCode())
                .headCode(department.getHeadCode())
                .level(department.getLevel())
                .isDeleted(department.getIsDeleted())
                .employeeCount(employeeCount)
                .build();
    }

    private EmployeeResponse toEmployeeResponse(Employees employee) {
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
                .positionCode(employee.getPositionCode())
                .employmentType(employee.getEmploymentType())
                .hireDate(employee.getHireDate())
                .contractEnd(employee.getContractEnd())
                .status(employee.getStatus())
                .isDeleted(employee.getIsDeleted())
                .build();
    }
}
