package com.example.employee_service.common.exception;

import com.example.employee_service.common.response.DomainCode;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(Object... args) {
        super(DomainCode.NOT_FOUND, args);
    }
}