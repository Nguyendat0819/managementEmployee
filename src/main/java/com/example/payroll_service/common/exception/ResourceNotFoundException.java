package com.example.payroll_service.common.exception;

import com.example.payroll_service.common.response.DomainCode;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(Object... args) {
        super(DomainCode.NOT_FOUND, args);
    }
}