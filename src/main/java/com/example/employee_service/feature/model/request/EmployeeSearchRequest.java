package com.example.employee_service.feature.model.request;

import com.example.employee_service.common.persistence.dto.PageDto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchRequest extends PageDto {
    @Size(max = 255)
    private String departmentCode;
    @Size(max = 255)
    private String positionCode;
    @Size(max = 50)
    private String status;
    @Size(max = 50)
    private String employmentType;
}
