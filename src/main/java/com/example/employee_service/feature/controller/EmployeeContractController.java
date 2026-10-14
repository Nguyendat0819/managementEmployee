package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.common.response.PageResponse;
import com.example.employee_service.feature.controller.api.EmployeeContractApi;
import com.example.employee_service.feature.model.request.EmployeeContractCreateRequest;
import com.example.employee_service.feature.model.request.EmployeeContractSearchRequest;
import com.example.employee_service.feature.model.request.EmployeeContractUpdateRequest;
import com.example.employee_service.feature.model.response.EmployeeContractResponse;
import com.example.employee_service.feature.service.EmployeeContractService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeContractController implements EmployeeContractApi {

    EmployeeContractService contractService;
    ApiResponseFactory apiResponseFactory;

    @Override
    public ApiResponse<EmployeeContractResponse> getCurrentContract(Jwt jwt) {
        return apiResponseFactory.success(contractService.getCurrentContract(resolveUserId(jwt), jwt.getClaimAsString("email")));
    }

    @Override
    public ApiResponse<PageResponse<EmployeeContractResponse>> searchContracts(EmployeeContractSearchRequest request) {
        return apiResponseFactory.success(contractService.searchContracts(request));
    }

    @Override
    public ApiResponse<EmployeeContractResponse> getContract(Long id) {
        return apiResponseFactory.success(contractService.getContract(id));
    }

    @Override
    public ApiResponse<EmployeeContractResponse> createContract(EmployeeContractCreateRequest request) {
        return apiResponseFactory.success(contractService.createContract(request));
    }

    @Override
    public ApiResponse<EmployeeContractResponse> updateContract(Long id, EmployeeContractUpdateRequest request) {
        return apiResponseFactory.success(contractService.updateContract(id, request));
    }

    @Override
    public ApiResponse<Void> deleteContract(Long id) {
        contractService.deleteContract(id);
        return apiResponseFactory.success();
    }

    private Long resolveUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
    }
}
