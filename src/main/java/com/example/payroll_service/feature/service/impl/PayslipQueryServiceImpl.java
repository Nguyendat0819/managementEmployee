package com.example.payroll_service.feature.service.impl;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.exception.ResourceNotFoundException;
import com.example.payroll_service.common.response.ApiResponse;
import com.example.payroll_service.common.response.DomainCode;
import com.example.payroll_service.feature.client.AdminManagerClient;
import com.example.payroll_service.feature.entity.PayrollRun;
import com.example.payroll_service.feature.entity.Payslip;
import com.example.payroll_service.feature.model.response.EmployeeProfileResponse;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import com.example.payroll_service.feature.repository.PayrollRunRepository;
import com.example.payroll_service.feature.repository.PayslipRepository;
import com.example.payroll_service.feature.service.PayslipQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayslipQueryServiceImpl implements PayslipQueryService {
    private static final String PAID = "Đã trả";

    PayslipRepository payslipRepository;
    PayrollRunRepository runRepository;
    AdminManagerClient employeeClient;

    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponse> getCurrentEmployeePayslips() {
        String employeeCode = currentEmployeeCode();
        return payslipRepository.findByEmployeeCodeIgnoreCaseOrderByRunDesc(employeeCode).stream()
                .map(slip -> toPaidPayslip(slip, employeeCode))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(PayslipResponse::periodYear)
                        .thenComparing(PayslipResponse::periodMonth)
                        .reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipResponse getCurrentEmployeePayslip(Long id) {
        String employeeCode = currentEmployeeCode();
        Payslip slip = payslipRepository.findByIdAndEmployeeCodeIgnoreCase(id, employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("phiếu lương " + id));
        PayslipResponse response = toPaidPayslip(slip, employeeCode);
        if (response == null) {
            throw new ResourceNotFoundException("phiếu lương " + id);
        }
        return response;
    }

    private String currentEmployeeCode() {
        try {
            ApiResponse<EmployeeProfileResponse> response = employeeClient.getCurrentEmployee();
            String employeeCode = response == null || response.getData() == null
                    ? null
                    : response.getData().employeeCode();
            if (employeeCode == null || employeeCode.isBlank()) {
                throw new BusinessException(DomainCode.NOT_FOUND, "hồ sơ nhân viên của tài khoản hiện tại");
            }
            return employeeCode.trim();
        } catch (RestClientException exception) {
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "employee profile");
        }
    }

    private PayslipResponse toPaidPayslip(Payslip slip, String employeeCode) {
        PayrollRun run = runRepository.findById(slip.getRun())
                .orElseThrow(() -> new ResourceNotFoundException("kỳ lương " + slip.getRun()));
        // Chỉ công khai phiếu sau khi kỳ lương đã được đánh dấu chi trả.
        if (!PAID.equals(run.getStatus())) {
            return null;
        }
        BigDecimal gross = zeroIfNull(slip.getGrossSalary());
        BigDecimal deductions = zeroIfNull(slip.getSocialInsurance())
                .add(zeroIfNull(slip.getHealthInsurance()))
                .add(zeroIfNull(slip.getUnemploymentInsurance()))
                .add(zeroIfNull(slip.getPitDeduction()))
                .add(zeroIfNull(slip.getOtherDeduction()));
        BigDecimal employerInsuranceTotal = zeroIfNull(slip.getEmployerSocialInsurance())
                .add(zeroIfNull(slip.getEmployerHealthInsurance()))
                .add(zeroIfNull(slip.getEmployerUnemploymentInsurance()));
        java.time.LocalDate periodStart = java.time.LocalDate.of(run.getPeriodYear(), run.getPeriodMonth(), 1);
        java.time.LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        return new PayslipResponse(
                slip.getId(), run.getId(), employeeCode, run.getPeriodYear(), run.getPeriodMonth(),
                periodStart, periodEnd, "Đã thanh toán", run.getPaidAt(),
                zeroIfNull(slip.getBaseSalary()), zeroIfNull(slip.getTotalAllowance()),
                zeroIfNull(slip.getOvertimePay()), zeroIfNull(slip.getBonus()), gross,
                zeroIfNull(slip.getSocialInsurance()), zeroIfNull(slip.getHealthInsurance()),
                zeroIfNull(slip.getUnemploymentInsurance()),
                zeroIfNull(slip.getPitDeduction()), zeroIfNull(slip.getOtherDeduction()),
                deductions, zeroIfNull(slip.getNetSalary()),
                zeroIfNull(slip.getEmployerSocialInsurance()), zeroIfNull(slip.getEmployerHealthInsurance()),
                zeroIfNull(slip.getEmployerUnemploymentInsurance()), employerInsuranceTotal, slip.getWorkingDays(),
                slip.getAbsentDays(), zeroIfNull(slip.getOvertimeHours()), slip.getSnapshotData()
        );
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
