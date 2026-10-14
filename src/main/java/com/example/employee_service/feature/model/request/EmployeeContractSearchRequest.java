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
public class EmployeeContractSearchRequest extends PageDto {

    @Size(max = 50)
    private String contractType;

    @Size(max = 255)
    private String employeeCode;

    @Size(max = 50)
    private String employmentType;

    @Size(max = 50)
    private String status;

    private Boolean isDeleted;
}
