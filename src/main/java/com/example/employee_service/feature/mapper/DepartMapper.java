package com.example.employee_service.feature.mapper;

import com.example.employee_service.feature.entity.Departments;
import com.example.employee_service.feature.model.request.DepartmentsRequest;
import com.example.employee_service.feature.model.request.DepartmentsUpdateRequest;
import com.example.employee_service.feature.model.response.DepartmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartMapper {

    Departments toEntity(DepartmentsRequest request);

    @Mapping(target = "employeeCount", ignore = true)
    DepartmentResponse toResponse(Departments department);

    Departments updateEntity(
            DepartmentsUpdateRequest request,
            @MappingTarget Departments department
    );
}
