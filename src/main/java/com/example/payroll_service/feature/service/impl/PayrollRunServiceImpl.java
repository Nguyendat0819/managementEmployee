package com.example.payroll_service.feature.service.impl;

import com.example.payroll_service.common.exception.BusinessException;
import com.example.payroll_service.common.exception.ResourceNotFoundException;
import com.example.payroll_service.common.response.DomainCode;
import com.example.payroll_service.common.util.SecurityUtil;
import com.example.payroll_service.feature.client.AttendancePayrollClient;
import com.example.payroll_service.feature.client.EmployeePayrollClient;
import com.example.payroll_service.feature.entity.PayrollHoliday;
import com.example.payroll_service.feature.entity.PayrollRun;
import com.example.payroll_service.feature.entity.Payslip;
import com.example.payroll_service.feature.entity.SalaryStructure;
import com.example.payroll_service.feature.model.request.PayrollAttendanceSummaryRequest;
import com.example.payroll_service.feature.model.request.PayrollEmployeePeriod;
import com.example.payroll_service.feature.model.request.PayrollRunRequest;
import com.example.payroll_service.feature.model.request.PayslipLineRequest;
import com.example.payroll_service.feature.model.response.PayrollAttendanceSummaryResponse;
import com.example.payroll_service.feature.model.response.PayrollCalculationResponse;
import com.example.payroll_service.feature.model.response.PayrollEmployeeResponse;
import com.example.payroll_service.feature.model.response.PayrollRunResponse;
import com.example.payroll_service.feature.model.response.PayslipResponse;
import com.example.payroll_service.feature.repository.PayrollHolidayRepository;
import com.example.payroll_service.feature.repository.PayrollRunRepository;
import com.example.payroll_service.feature.repository.PayslipRepository;
import com.example.payroll_service.feature.repository.SalaryStructureRepository;
import com.example.payroll_service.feature.service.PayrollRunService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayrollRunServiceImpl implements PayrollRunService {
    private static final String DRAFT = "Nháp";
    private static final String PENDING_APPROVAL = "Chờ duyệt";
    private static final String APPROVED = "Đã duyệt";
    private static final String PAID = "Đã trả";
    private static final String CALCULATION_ASSUMPTIONS = "Thuế giả định cá nhân cư trú, không có người phụ thuộc; tăng ca được giả định hợp lệ theo giới hạn luật; bảo hiểm lấy từ lương hợp đồng và phụ cấp cấu trúc lương. Mức trần BHTN mặc định theo Vùng I, có thể cấu hình theo địa bàn làm việc.";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    PayrollRunRepository runRepository;
    PayslipRepository payslipRepository;
    EmployeePayrollClient employeeClient;
    AttendancePayrollClient attendanceClient;
    PayrollHolidayRepository holidayRepository;
    SalaryStructureRepository salaryStructureRepository;
    VietnamPayrollCalculator calculator;

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
        run.setTotalEmployerCost(BigDecimal.ZERO);
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
    public PayrollCalculationResponse calculate(Long runId) {
        PayrollRun run = findRun(runId);
        requireStatus(run, DRAFT);
        if (!payslipRepository.findByRunOrderByEmployeeCodeAsc(runId).isEmpty()) {
            throw new BusinessException(DomainCode.CONFLICT,
                    "kỳ đã có phiếu lương; tạo kỳ nháp trống để chạy tính tự động, dữ liệu hiện tại được giữ nguyên");
        }

        LocalDate periodStart = LocalDate.of(run.getPeriodYear(), run.getPeriodMonth(), 1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        List<PayrollEmployeeResponse> candidates = fetchEligibleEmployees(periodStart, periodEnd);
        if (candidates.isEmpty()) {
            throw new BusinessException(DomainCode.BAD_REQUEST, "không có nhân viên có hợp đồng hiệu lực trong kỳ lương");
        }

        List<PayrollHoliday> holidays = holidayRepository
                .findAllByHolidayDateLessThanEqualAndHolidayDateEndGreaterThanEqual(periodEnd, periodStart);
        List<LocalDate> holidayDates = expandHolidayDates(holidays, periodStart, periodEnd);
        List<PayrollEmployeePeriod> employeePeriods = candidates.stream()
                .filter(candidate -> candidate.contractStartDate() != null)
                .map(candidate -> new PayrollEmployeePeriod(candidate.employeeCode(),
                        candidate.contractStartDate().isBefore(periodStart) ? periodStart : candidate.contractStartDate(),
                        candidate.contractEndDate() == null || candidate.contractEndDate().isAfter(periodEnd)
                                ? periodEnd : candidate.contractEndDate()))
                .filter(period -> !period.from().isAfter(period.to()))
                .toList();
        List<PayrollAttendanceSummaryResponse> attendanceSummaries = fetchAttendance(periodStart, periodEnd,
                employeePeriods, holidayDates);
        Map<String, PayrollAttendanceSummaryResponse> attendanceByCode = attendanceSummaries.stream()
                .collect(Collectors.toMap(summary -> normalizeCode(summary.employeeCode()), summary -> summary,
                        (first, ignored) -> first));
        List<SalaryStructure> structures = salaryStructureRepository.findAllByOrderByEffectiveDateDescPositionCodeAsc();
        Map<String, List<PayrollEmployeeResponse>> employeeContracts = candidates.stream()
                .collect(Collectors.groupingBy(candidate -> normalizeCode(candidate.employeeCode()), LinkedHashMap::new,
                        Collectors.toList()));

        List<Payslip> slips = new ArrayList<>(employeeContracts.size());
        List<String> warnings = new ArrayList<>();
        warnings.add(CALCULATION_ASSUMPTIONS);
        for (Map.Entry<String, List<PayrollEmployeeResponse>> entry : employeeContracts.entrySet()) {
            List<PayrollEmployeeResponse> employeeRows = entry.getValue();
            String employeeCode = employeeRows.get(0).employeeCode();
            PayrollAttendanceSummaryResponse attendance = attendanceByCode.get(entry.getKey());
            if (attendance == null) {
                warnings.add(employeeCode + ": không nhận được dữ liệu tổng hợp chấm công.");
                continue;
            }
            warnings.addAll(attendance.warnings().stream()
                    .map(warning -> employeeCode + ": " + warning).toList());

            MonthlyPay monthlyPay = monthlyPay(employeeRows, structures, periodStart, periodEnd);
            warnings.addAll(monthlyPay.warnings().stream()
                    .map(warning -> employeeCode + ": " + warning).toList());
            if (monthlyPay.baseSalary().signum() <= 0) {
                warnings.add(employeeCode + ": thiếu lương hợp đồng và cấu trúc lương hiệu lực; bỏ qua phiếu.");
                continue;
            }

            VietnamPayrollCalculator.Result result = calculator.calculate(monthlyPay.baseSalary(),
                    monthlyPay.allowance(), monthlyPay.insuranceBase(), attendance, periodStart, 0);
            List<String> employeeWarnings = new ArrayList<>(attendance.warnings());
            employeeWarnings.addAll(monthlyPay.warnings());
            employeeWarnings.add(CALCULATION_ASSUMPTIONS);
            if (attendance.standardWorkingMinutes() <= 0 && attendance.regularOvertimeMinutes()
                    + attendance.restDayOvertimeMinutes() + attendance.holidayOvertimeMinutes() > 0) {
                String overtimeWarning = "Có phút tăng ca nhưng thiếu giờ chuẩn để quy đổi ra tiền.";
                warnings.add(employeeCode + ": " + overtimeWarning);
                employeeWarnings.add(overtimeWarning);
            }
            slips.add(toCalculatedPayslip(runId, employeeRows, attendance, monthlyPay, result, periodStart,
                    employeeWarnings));
        }

        if (slips.isEmpty()) {
            throw new BusinessException(DomainCode.BAD_REQUEST, "không thể tạo phiếu lương do thiếu dữ liệu lương hợp lệ");
        }
        payslipRepository.saveAll(slips);
        refreshTotals(run);
        run.setSalaryStructureLabel("Tính tự động " + periodStart.getYear());
        runRepository.save(run);
        return new PayrollCalculationResponse(toResponse(run), warnings.stream().distinct().toList());
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
        BigDecimal grossSalary = entry.baseSalary()
                .add(entry.totalAllowance())
                .add(entry.overtimePay())
                .add(entry.bonus());
        BigDecimal netSalary = grossSalary
                .subtract(entry.socialInsurance())
                .subtract(entry.healthInsurance())
                .subtract(entry.unemploymentInsurance())
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
        slip.setUnemploymentInsurance(entry.unemploymentInsurance());
        slip.setEmployerSocialInsurance(BigDecimal.ZERO);
        slip.setEmployerHealthInsurance(BigDecimal.ZERO);
        slip.setEmployerUnemploymentInsurance(BigDecimal.ZERO);
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
        run.setTotalEmployerCost(slips.stream().map(slip -> zeroIfNull(slip.getGrossSalary())
                        .add(zeroIfNull(slip.getEmployerSocialInsurance()))
                        .add(zeroIfNull(slip.getEmployerHealthInsurance()))
                        .add(zeroIfNull(slip.getEmployerUnemploymentInsurance())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private List<PayrollEmployeeResponse> fetchEligibleEmployees(LocalDate from, LocalDate to) {
        try {
            var response = employeeClient.getEligibleEmployees(from, to);
            if (response == null || response.getData() == null) {
                throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "employee roster");
            }
            return response.getData();
        } catch (RestClientException exception) {
            log.error("Employee roster request failed", exception);
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "employee roster");
        }
    }

    private List<PayrollAttendanceSummaryResponse> fetchAttendance(LocalDate from, LocalDate to,
                                                                   List<PayrollEmployeePeriod> periods,
                                                                   List<LocalDate> holidays) {
        if (periods.isEmpty()) {
            throw new BusinessException(DomainCode.BAD_REQUEST, "không có hợp đồng đủ ngày hiệu lực để tính công");
        }
        try {
            var response = attendanceClient.payrollSummary(
                    new PayrollAttendanceSummaryRequest(from, to, periods, holidays));
            if (response == null || response.getData() == null) {
                throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "attendance summary");
            }
            return response.getData();
        } catch (RestClientException exception) {
            throw new BusinessException(DomainCode.EXTERNAL_SERVICE_ERROR, "attendance summary");
        }
    }

    private List<LocalDate> expandHolidayDates(List<PayrollHoliday> holidays, LocalDate periodStart,
                                                LocalDate periodEnd) {
        List<LocalDate> dates = new ArrayList<>();
        for (PayrollHoliday holiday : holidays) {
            LocalDate from = holiday.getHolidayDate().isBefore(periodStart) ? periodStart : holiday.getHolidayDate();
            LocalDate to = holiday.getHolidayDateEnd().isAfter(periodEnd) ? periodEnd : holiday.getHolidayDateEnd();
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                dates.add(date);
            }
        }
        return dates;
    }

    private MonthlyPay monthlyPay(List<PayrollEmployeeResponse> contracts, List<SalaryStructure> structures,
                                  LocalDate periodStart, LocalDate periodEnd) {
        BigDecimal monthlyBase = BigDecimal.ZERO;
        BigDecimal monthlyAllowance = BigDecimal.ZERO;
        BigDecimal insuranceBase = BigDecimal.ZERO;
        List<String> warnings = new ArrayList<>();
        int periodDays = periodStart.lengthOfMonth();
        Set<String> overlapWarnings = new HashSet<>();

        for (LocalDate date = periodStart; !date.isAfter(periodEnd); date = date.plusDays(1)) {
            LocalDate workDate = date;
            List<PayrollEmployeeResponse> active = contracts.stream()
                    .filter(contract -> contract.contractStartDate() != null
                            && !contract.contractStartDate().isAfter(workDate)
                            && (contract.contractEndDate() == null || !contract.contractEndDate().isBefore(workDate)))
                    .sorted(Comparator.comparing(PayrollEmployeeResponse::contractStartDate).reversed())
                    .toList();
            if (active.isEmpty()) {
                continue;
            }
            if (active.size() > 1 && overlapWarnings.add(date.toString())) {
                warnings.add("Có nhiều hợp đồng hiệu lực ngày " + date + "; dùng hợp đồng bắt đầu gần nhất.");
            }
            PayrollEmployeeResponse contract = active.get(0);
            SalaryStructure structure = structureFor(structures, contract.positionCode(), date);
            BigDecimal dailyMonthlyBase = positive(contract.actualSalary())
                    ? contract.actualSalary()
                    : structure == null ? BigDecimal.ZERO : zeroIfNull(structure.getBaseSalary());
            BigDecimal dailyAllowance = structure == null ? BigDecimal.ZERO : allowanceTotal(structure);
            insuranceBase = dailyMonthlyBase.add(dailyAllowance);
            if (dailyMonthlyBase.signum() > 0) {
                monthlyBase = monthlyBase.add(dailyMonthlyBase.divide(BigDecimal.valueOf(periodDays), 8,
                        java.math.RoundingMode.HALF_UP));
            }
            if (dailyAllowance.signum() > 0) {
                monthlyAllowance = monthlyAllowance.add(dailyAllowance
                        .divide(BigDecimal.valueOf(periodDays), 8, java.math.RoundingMode.HALF_UP));
            }
        }
        return new MonthlyPay(money(monthlyBase), money(monthlyAllowance), money(insuranceBase), warnings);
    }

    private SalaryStructure structureFor(List<SalaryStructure> structures, String positionCode, LocalDate date) {
        if (positionCode == null || positionCode.isBlank()) {
            return null;
        }
        return structures.stream()
                .filter(structure -> structure.getPositionCode().equalsIgnoreCase(positionCode))
                .filter(structure -> !structure.getEffectiveDate().isAfter(date))
                .filter(structure -> "Đang áp dụng".equals(structure.getFormulaConfig().path("status").asText("")))
                .max(Comparator.comparing(SalaryStructure::getEffectiveDate))
                .orElse(null);
    }

    private BigDecimal allowanceTotal(SalaryStructure structure) {
        if (structure.getAllowances() == null || structure.getAllowances().path("total").isMissingNode()) {
            return BigDecimal.ZERO;
        }
        return structure.getAllowances().path("total").decimalValue();
    }

    private Payslip toCalculatedPayslip(Long runId, List<PayrollEmployeeResponse> contracts,
                                        PayrollAttendanceSummaryResponse attendance, MonthlyPay monthlyPay,
                                        VietnamPayrollCalculator.Result result, LocalDate periodStart,
                                        List<String> employeeWarnings) {
        PayrollEmployeeResponse employee = contracts.stream()
                .max(Comparator.comparing(PayrollEmployeeResponse::contractStartDate)).orElseThrow();
        Payslip slip = new Payslip();
        slip.setRun(runId);
        slip.setEmployeeCode(employee.employeeCode());
        slip.setBaseSalary(result.baseSalary());
        slip.setTotalAllowance(result.totalAllowance());
        slip.setOvertimePay(result.overtimePay());
        slip.setBonus(BigDecimal.ZERO);
        slip.setGrossSalary(result.grossSalary());
        slip.setSocialInsurance(result.socialInsurance());
        slip.setHealthInsurance(result.healthInsurance());
        slip.setUnemploymentInsurance(result.unemploymentInsurance());
        slip.setEmployerSocialInsurance(result.employerSocialInsurance());
        slip.setEmployerHealthInsurance(result.employerHealthInsurance());
        slip.setEmployerUnemploymentInsurance(result.employerUnemploymentInsurance());
        slip.setPitDeduction(result.pitDeduction());
        slip.setOtherDeduction(BigDecimal.ZERO);
        slip.setNetSalary(result.netSalary());
        slip.setWorkingDays((short) Math.min(attendance.workedDays(), Short.MAX_VALUE));
        slip.setAbsentDays((short) Math.min(attendance.absentDays(), Short.MAX_VALUE));
        slip.setOvertimeHours(result.overtimeHours());

        ObjectNode snapshot = JsonNodeFactory.instance.objectNode();
        snapshot.put("calculationSource", "automatic");
        snapshot.put("calculationPolicy", periodStart.getYear() >= 2026 ? "VN-2026" : "VN-legacy");
        snapshot.put("employeeName", employee.fullName());
        snapshot.put("departmentCode", employee.departmentCode());
        snapshot.put("positionCode", employee.positionCode());
        snapshot.put("contractMonthlySalary", monthlyPay.baseSalary());
        snapshot.put("contractMonthlyAllowance", monthlyPay.allowance());
        snapshot.put("scheduledDays", attendance.scheduledDays());
        snapshot.put("workedDays", attendance.workedDays());
        snapshot.put("absentDays", attendance.absentDays());
        snapshot.put("paidHolidayDays", attendance.paidHolidayDays());
        snapshot.put("standardWorkingMinutes", attendance.standardWorkingMinutes());
        snapshot.put("regularOvertimeMinutes", attendance.regularOvertimeMinutes());
        snapshot.put("restDayOvertimeMinutes", attendance.restDayOvertimeMinutes());
        snapshot.put("holidayOvertimeMinutes", attendance.holidayOvertimeMinutes());
        snapshot.put("paidRatio", result.paidRatio());
        snapshot.put("insuranceBase", result.insuranceBase());
        snapshot.put("unemploymentInsuranceBase", result.unemploymentInsuranceBase());
        snapshot.put("taxableIncome", result.taxableIncome());
        snapshot.put("taxBase", result.taxBase());
        snapshot.put("overtimeTaxExempt", result.overtimeTaxExempt());
        snapshot.put("taxResident", true);
        snapshot.put("dependentCount", 0);
        snapshot.put("allAllowancesTreatedAsTaxableAndInsurable", true);
        snapshot.put("employeeSocialInsuranceRate", "8%");
        snapshot.put("employeeHealthInsuranceRate", "1.5%");
        snapshot.put("employeeUnemploymentInsuranceRate", "1%");
        snapshot.put("employerSocialInsuranceRate", "17.5%");
        snapshot.put("employerHealthInsuranceRate", "3%");
        snapshot.put("employerUnemploymentInsuranceRate", "1%");
        snapshot.set("calculationWarnings", JsonNodeFactory.instance.arrayNode().addAll(
                employeeWarnings.stream().map(JsonNodeFactory.instance::textNode).toList()));
        slip.setSnapshotData(snapshot);
        return slip;
    }

    private boolean positive(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(0, java.math.RoundingMode.HALF_UP);
    }

    private String normalizeCode(String employeeCode) {
        return employeeCode == null ? "" : employeeCode.trim().toLowerCase(Locale.ROOT);
    }

    private record MonthlyPay(BigDecimal baseSalary, BigDecimal allowance, BigDecimal insuranceBase,
                              List<String> warnings) {
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
                zeroIfNull(run.getTotalEmployerCost()),
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
                .add(zeroIfNull(slip.getUnemploymentInsurance()))
                .add(zeroIfNull(slip.getPitDeduction()))
                .add(zeroIfNull(slip.getOtherDeduction()));
        BigDecimal employerInsuranceTotal = zeroIfNull(slip.getEmployerSocialInsurance())
                .add(zeroIfNull(slip.getEmployerHealthInsurance()))
                .add(zeroIfNull(slip.getEmployerUnemploymentInsurance()));
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
                zeroIfNull(slip.getUnemploymentInsurance()),
                zeroIfNull(slip.getPitDeduction()),
                zeroIfNull(slip.getOtherDeduction()),
                deductions,
                zeroIfNull(slip.getNetSalary()),
                zeroIfNull(slip.getEmployerSocialInsurance()),
                zeroIfNull(slip.getEmployerHealthInsurance()),
                zeroIfNull(slip.getEmployerUnemploymentInsurance()),
                employerInsuranceTotal,
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
