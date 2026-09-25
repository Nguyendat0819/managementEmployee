package com.example.employee_service.common.exception;

import com.example.employee_service.common.response.DomainCode;

public class BusinessException extends BaseException {
    public BusinessException(DomainCode domainCode, Object ... args) {
        super(domainCode, args);
    }
}