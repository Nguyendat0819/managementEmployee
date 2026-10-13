package com.example.employee_service.feature.model.request;

import com.example.employee_service.common.persistence.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionSearchRequest extends PageDto {
    private Boolean isDeleted;
}
