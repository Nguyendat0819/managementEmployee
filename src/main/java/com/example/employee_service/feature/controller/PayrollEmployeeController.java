package com.example.employee_service.feature.controller;

import com.example.employee_service.common.response.ApiResponse;
import com.example.employee_service.common.response.ApiResponseFactory;
import com.example.employee_service.common.exception.BusinessException;
import com.example.employee_service.common.response.DomainCode;
import com.example.employee_service.feature.controller.api.PayrollEmployeeApi;
import com.example.employee_service.feature.model.response.PayrollEmployeeResponse;
import com.example.employee_service.feature.repository.EmployeeContractsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayrollEmployeeController implements PayrollEmployeeApi {
    private final EmployeeContractsRepository contractsRepository;
    private final ApiResponseFactory responseFactory;

    @Override
    public ApiResponse<List<PayrollEmployeeResponse>> getEligibleEmployees(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BusinessException(DomainCode.INVALID_PARAMETER, "from/to");
        }
        List<PayrollEmployeeResponse> employees = contractsRepository.findPayrollContracts(from, to).stream()
                .map(contract -> new PayrollEmployeeResponse(
                        contract.getEmployeeCode(), contract.getFullName(),
                        contract.getDepartmentCode(), contract.getDepartmentName(),
                        contract.getPositionCode(), contract.getPositionName(), contract.getActualSalary(),
                        contract.getEmploymentType(), contract.getContractType(),
                        contract.getStartDate(), contract.getEndDate()))
                .toList();
        return responseFactory.success(employees);
    }
}
