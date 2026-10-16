package com.example.payroll_service.feature.service.impl;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.exception.ResourceNotFoundException;
import com.example.payroll_service.common.response.DomainCode;
import com.example.payroll_service.common.util.SecurityUtil;
import com.example.payroll_service.feature.entity.PayrollRun;
import com.example.payroll_service.feature.entity.Payslip;
import com.example.payroll_service.feature.model.request.PayrollRunRequest;
import com.example.payroll_service.feature.model.request.PayslipLineRequest;
import com.example.payroll_service.feature.model.response.PayrollRunResponse;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import com.example.payroll_service.feature.repository.PayrollRunRepository;
import com.example.payroll_service.feature.repository.PayslipRepository;
import com.example.payroll_service.feature.service.PayrollRunService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayrollRunServiceImpl implements PayrollRunService {
    private static final String DRAFT = "Nháp";
    private static final String PENDING_APPROVAL = "Chờ duyệt";
    private static final String APPROVED = "Đã duyệt";
    private static final String PAID = "Đã trả";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    PayrollRunRepository runRepository;
    PayslipRepository payslipRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollRunResponse> getAll() {
        return runRepository.findAllByOrderByPeriodYearDescPeriodMonthDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollRunResponse getById(Long id) {
        return toResponse(findRun(id));
    }

    @Override
    @Transactional
    public PayrollRunResponse create(PayrollRunRequest request) {
        short year = request.periodYear().shortValue();
        if (runRepository.existsByPeriodYearAndPeriodMonth(year, request.periodMonth())) {
            throw new BusinessException(DomainCode.CONFLICT, "kỳ lương " + request.periodMonth() + "/" + year);
        }

        PayrollRun run = new PayrollRun();
        run.setPeriodYear(year);
        run.setPeriodMonth(request.periodMonth());
        run.setSalaryStructureLabel("Khung lương " + year);
        run.setEmployeeCount(0);
        run.setTotalGross(BigDecimal.ZERO);
        run.setTotalNet(BigDecimal.ZERO);
        run.setStatus(DRAFT);
        run.setNote(request.note() == null ? "" : request.note().trim());
        return toResponse(runRepository.save(run));
    }

    @Override
    @Transactional
    public void deleteDraft(Long id) {
        PayrollRun run = findRun(id);
        requireStatus(run, DRAFT);
        payslipRepository.deleteByRun(id);
        runRepository.delete(run);
    }

    @Override
    @Transactional
    public List<PayslipResponse> addPayslips(Long runId, List<PayslipLineRequest> entries) {
        PayrollRun run = findRun(runId);
        requireStatus(run, DRAFT);

        Set<String> employeeCodes = new HashSet<>();
        List<Payslip> slips = new ArrayList<>(entries.size());
        for (PayslipLineRequest entry : entries) {
            String employeeCode = entry.employeeCode().trim();
            String normalizedCode = employeeCode.toLowerCase(Locale.ROOT);
            if (!employeeCodes.add(normalizedCode)
                    || payslipRepository.existsByRunAndEmployeeCodeIgnoreCase(runId, employeeCode)) {
                throw new BusinessException(DomainCode.CONFLICT, "phiếu lương của nhân viên " + employeeCode + " trong kỳ này");
            }
            slips.add(toEntity(runId, employeeCode, entry));
        }

        List<Payslip> saved = payslipRepository.saveAll(slips);
        refreshTotals(run);
        return saved.stream().map(slip -> toPayslipResponse(slip, run)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponse> getPayslips(Long runId) {
        PayrollRun run = findRun(runId);
        return payslipRepository.findByRunOrderByEmployeeCodeAsc(runId).stream()
                .map(slip -> toPayslipResponse(slip, run))
                .toList();
    }

    @Override
    @Transactional
    public PayrollRunResponse submitForApproval(Long id) {
        PayrollRun run = findRun(id);
        requireStatus(run, DRAFT);
        if (payslipRepository.findByRunOrderByEmployeeCodeAsc(id).isEmpty()) {
            throw new BusinessException(DomainCode.BAD_REQUEST, "kỳ lương cần có ít nhất một phiếu lương trước khi gửi duyệt");
        }
        run.setStatus(PENDING_APPROVAL);
        refreshTotals(run);
        return toResponse(runRepository.save(run));
    }

    @Override
    @Transactional
    public PayrollRunResponse approve(Long id) {
        PayrollRun run = findRun(id);
        requireStatus(run, PENDING_APPROVAL);
        run.setStatus(APPROVED);
        run.setApprovedBy(SecurityUtil.getCurrentUsername());
        run.setApprovedAt(LocalDateTime.now(BUSINESS_ZONE));
        return toResponse(runRepository.save(run));
    }

    @Override
    @Transactional
    public PayrollRunResponse markPaid(Long id) {
        PayrollRun run = findRun(id);
        requireStatus(run, APPROVED);
        run.setStatus(PAID);
        run.setPaidAt(LocalDateTime.now(BUSINESS_ZONE));
        return toResponse(runRepository.save(run));
    }

    private Payslip toEntity(Long runId, String employeeCode, PayslipLineRequest entry) {
        // Caller chịu trách nhiệm tính chính sách công, thuế và hệ số; payroll chỉ cộng các khoản đã gửi.
        BigDecimal grossSalary = entry.baseSalary()
                .add(entry.totalAllowance())
                .add(entry.overtimePay())
                .add(entry.bonus());
        BigDecimal netSalary = grossSalary
                .subtract(entry.socialInsurance())
                .subtract(entry.healthInsurance())
                .subtract(entry.pitDeduction())
                .subtract(entry.otherDeduction());

        Payslip slip = new Payslip();
        slip.setRun(runId);
        slip.setEmployeeCode(employeeCode);
        slip.setBaseSalary(entry.baseSalary());
        slip.setTotalAllowance(entry.totalAllowance());
        slip.setOvertimePay(entry.overtimePay());
        slip.setBonus(entry.bonus());
        slip.setGrossSalary(grossSalary);
        slip.setSocialInsurance(entry.socialInsurance());
        slip.setHealthInsurance(entry.healthInsurance());
        slip.setPitDeduction(entry.pitDeduction());
        slip.setOtherDeduction(entry.otherDeduction());
        slip.setNetSalary(netSalary);
        slip.setWorkingDays(entry.workingDays());
        slip.setAbsentDays(entry.absentDays());
        slip.setOvertimeHours(entry.overtimeHours());
        slip.setSnapshotData(entry.snapshotData() == null ? JsonNodeFactory.instance.objectNode() : entry.snapshotData());
        return slip;
    }

    private void refreshTotals(PayrollRun run) {
        List<Payslip> slips = payslipRepository.findByRunOrderByEmployeeCodeAsc(run.getId());
        run.setEmployeeCount(slips.size());
        run.setTotalGross(slips.stream().map(Payslip::getGrossSalary).reduce(BigDecimal.ZERO, BigDecimal::add));
        run.setTotalNet(slips.stream().map(Payslip::getNetSalary).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private PayrollRun findRun(Long id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("kỳ lương " + id));
    }

    private void requireStatus(PayrollRun run, String expected) {
        if (!expected.equals(run.getStatus())) {
            throw new BusinessException(DomainCode.CONFLICT, "kỳ lương " + run.getPeriodMonth() + "/" + run.getPeriodYear() + " không ở trạng thái " + expected);
        }
    }

    private PayrollRunResponse toResponse(PayrollRun run) {
        BigDecimal gross = zeroIfNull(run.getTotalGross());
        BigDecimal net = zeroIfNull(run.getTotalNet());
        return new PayrollRunResponse(
                run.getId(),
                run.getPeriodYear(),
                run.getPeriodMonth(),
                String.format(Locale.ROOT, "Tháng %02d/%d", run.getPeriodMonth(), run.getPeriodYear()),
                run.getSalaryStructureLabel() == null ? "Khung lương " + run.getPeriodYear() : run.getSalaryStructureLabel(),
                run.getEmployeeCount() == null ? 0 : run.getEmployeeCount(),
                gross,
                gross.subtract(net),
                net,
                run.getStatus(),
                run.getCreatedAt(),
                run.getApprovedBy(),
                run.getApprovedAt(),
                run.getPaidAt(),
                run.getNote()
        );
    }

    private PayslipResponse toPayslipResponse(Payslip slip, PayrollRun run) {
        BigDecimal gross = zeroIfNull(slip.getGrossSalary());
        BigDecimal deductions = zeroIfNull(slip.getSocialInsurance())
                .add(zeroIfNull(slip.getHealthInsurance()))
                .add(zeroIfNull(slip.getPitDeduction()))
                .add(zeroIfNull(slip.getOtherDeduction()));
        LocalDate periodStart = LocalDate.of(run.getPeriodYear(), run.getPeriodMonth(), 1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        return new PayslipResponse(
                slip.getId(),
                run.getId(),
                slip.getEmployeeCode(),
                run.getPeriodYear(),
                run.getPeriodMonth(),
                periodStart,
                periodEnd,
                run.getStatus(),
                run.getPaidAt(),
                zeroIfNull(slip.getBaseSalary()),
                zeroIfNull(slip.getTotalAllowance()),
                zeroIfNull(slip.getOvertimePay()),
                zeroIfNull(slip.getBonus()),
                gross,
                zeroIfNull(slip.getSocialInsurance()),
                zeroIfNull(slip.getHealthInsurance()),
                zeroIfNull(slip.getPitDeduction()),
                zeroIfNull(slip.getOtherDeduction()),
                deductions,
                zeroIfNull(slip.getNetSalary()),
                slip.getWorkingDays(),
                slip.getAbsentDays(),
                zeroIfNull(slip.getOvertimeHours()),
                slip.getSnapshotData()
        );
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
