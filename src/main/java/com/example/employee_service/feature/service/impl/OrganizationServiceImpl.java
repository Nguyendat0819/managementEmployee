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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationServiceImpl implements OrganizationService {
    EmployeesRepository employeesRepository;
    DepartmentsRepository departmentsRepository;
    DepartMapper  departMapper;
    @Override
    public DepartmentResponse createDepartment(DepartmentsRequest request) {
//      Kiểm tra hàm đã xóa hay chưa
        Departments departments = departmentsRepository.findByDeptCodeAndIsDeleted(request.getDeptCode(), Const.IN_DELETED);
        if(departments != null) {
            throw new BusinessException(DomainCode.CONFLICT, departments.getName());
        }
        Departments newDepartments = departMapper.toEntity(request);
        newDepartments.setIsDeleted(Const.IN_DELETED);
        departmentsRepository.save(newDepartments);
        return toDepartmentResponse( newDepartments, 0);
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
        return toDepartmentResponse(updateDepartments, 0);
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
        Map<String, Long> employeeCounts = employees.stream()
                .filter(employee -> employee.getDepartmentCode() != null)
                .collect(Collectors.groupingBy(Employees::getDepartmentCode, Collectors.counting()));

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
                .map(department -> toDepartmentResponse(department, employeeCounts.getOrDefault(department.getDeptCode(), 0L)))
                .toList();
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(String departmentCode) {
        return employeesRepository.findAllByIsDeletedFalseAndDepartmentCodeOrderByFullNameAsc(departmentCode)
                .stream()
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
