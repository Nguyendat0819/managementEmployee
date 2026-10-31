package com.example.payroll_service.feature.service.impl;

import com.example.payroll_service.feature.model.response.PayrollAttendanceSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class VietnamPayrollCalculator {
    private static final BigDecimal EMPLOYEE_SOCIAL_RATE = new BigDecimal("0.08");
    private static final BigDecimal EMPLOYEE_HEALTH_RATE = new BigDecimal("0.015");
    private static final BigDecimal EMPLOYEE_UNEMPLOYMENT_RATE = new BigDecimal("0.01");
    private static final BigDecimal EMPLOYER_SOCIAL_RATE = new BigDecimal("0.175");
    private static final BigDecimal EMPLOYER_HEALTH_RATE = new BigDecimal("0.03");
    private static final BigDecimal EMPLOYER_UNEMPLOYMENT_RATE = new BigDecimal("0.01");
    private static final BigDecimal MONEY_ZERO = BigDecimal.ZERO;
    private static final BigDecimal VND = BigDecimal.ONE;

    @Value("${app.payroll.unemployment-region-minimum-2026:5310000}")
    private BigDecimal unemploymentRegionMinimum2026;

    @Value("${app.payroll.unemployment-region-minimum-2024-2025:4960000}")
    private BigDecimal unemploymentRegionMinimum2024To2025;

    public Result calculate(BigDecimal monthlyBase, BigDecimal monthlyAllowance, BigDecimal monthlyInsuranceBase,
                            PayrollAttendanceSummaryResponse attendance, LocalDate periodStart,
                            int dependentCount) {
        return calculate(monthlyBase, monthlyAllowance, monthlyInsuranceBase, attendance, periodStart,
                dependentCount, BigDecimal.ZERO);
    }

    public Result calculate(BigDecimal monthlyBase, BigDecimal monthlyAllowance, BigDecimal monthlyInsuranceBase,
                            PayrollAttendanceSummaryResponse attendance, LocalDate periodStart,
                            int dependentCount, BigDecimal approvedTaskBonus) {
        BigDecimal taskBonus = approvedTaskBonus == null ? BigDecimal.ZERO : approvedTaskBonus;
        BigDecimal paidRatio = paidRatio(attendance);
        BigDecimal earnedBase = money(monthlyBase.multiply(paidRatio));
        BigDecimal earnedAllowance = money(monthlyAllowance.multiply(paidRatio));
        int regularMinutes = attendance.regularOvertimeMinutes();
        int restMinutes = attendance.restDayOvertimeMinutes();
        int holidayMinutes = attendance.holidayOvertimeMinutes();
        int standardMinutes = attendance.standardWorkingMinutes();
        BigDecimal overtimePay = standardMinutes <= 0 ? MONEY_ZERO : money(
                hourlyRate(monthlyBase, standardMinutes)
                        .multiply(BigDecimal.valueOf(regularMinutes)).multiply(new BigDecimal("1.5"))
                        .add(hourlyRate(monthlyBase, standardMinutes)
                                .multiply(BigDecimal.valueOf(restMinutes)).multiply(new BigDecimal("2")))
                        .add(hourlyRate(monthlyBase, standardMinutes)
                                .multiply(BigDecimal.valueOf(holidayMinutes)).multiply(new BigDecimal("3"))));
        BigDecimal gross = money(earnedBase.add(earnedAllowance).add(overtimePay).add(taskBonus));

        BigDecimal insuranceBase = cappedInsuranceBase(monthlyInsuranceBase, periodStart);
        BigDecimal employeeSocial = money(insuranceBase.multiply(EMPLOYEE_SOCIAL_RATE));
        BigDecimal employeeHealth = money(insuranceBase.multiply(EMPLOYEE_HEALTH_RATE));
        BigDecimal unemploymentBase = cappedUnemploymentBase(monthlyInsuranceBase, periodStart);
        BigDecimal employeeUnemployment = money(unemploymentBase.multiply(EMPLOYEE_UNEMPLOYMENT_RATE));
        BigDecimal employerSocial = money(insuranceBase.multiply(EMPLOYER_SOCIAL_RATE));
        BigDecimal employerHealth = money(insuranceBase.multiply(EMPLOYER_HEALTH_RATE));
        BigDecimal employerUnemployment = money(unemploymentBase.multiply(EMPLOYER_UNEMPLOYMENT_RATE));

        BigDecimal overtimeTaxExempt = overtimeTaxExempt(monthlyBase, standardMinutes,
                regularMinutes, restMinutes, holidayMinutes, overtimePay, periodStart);
        BigDecimal taxableIncome = earnedBase.add(earnedAllowance).add(overtimePay).add(taskBonus).subtract(overtimeTaxExempt)
                .subtract(employeeSocial).subtract(employeeHealth).subtract(employeeUnemployment);
        BigDecimal personalDeduction = periodStart.getYear() >= 2026
                ? new BigDecimal("15500000") : new BigDecimal("11000000");
        BigDecimal dependentDeduction = periodStart.getYear() >= 2026
                ? new BigDecimal("6200000") : new BigDecimal("4400000");
        BigDecimal taxBase = taxableIncome.subtract(personalDeduction)
                .subtract(dependentDeduction.multiply(BigDecimal.valueOf(Math.max(dependentCount, 0))))
                .max(MONEY_ZERO);
        BigDecimal pit = progressiveTax(taxBase, periodStart.getYear());
        BigDecimal deductions = money(employeeSocial.add(employeeHealth).add(employeeUnemployment).add(pit));
        BigDecimal net = money(gross.subtract(deductions));
        BigDecimal employerCost = money(gross.add(employerSocial).add(employerHealth).add(employerUnemployment));
        BigDecimal overtimeHours = BigDecimal.valueOf(regularMinutes + restMinutes + holidayMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        return new Result(earnedBase, earnedAllowance, overtimePay, gross, employeeSocial, employeeHealth,
                employeeUnemployment, pit, net, employerSocial, employerHealth, employerUnemployment,
                employerCost, insuranceBase, unemploymentBase, taxableIncome.max(MONEY_ZERO), taxBase, overtimeHours, paidRatio,
                overtimeTaxExempt);
    }

    private BigDecimal paidRatio(PayrollAttendanceSummaryResponse attendance) {
        int payableDays = attendance.scheduledDays() + attendance.paidHolidayDays();
        if (payableDays == 0) {
            return BigDecimal.ONE;
        }
        return BigDecimal.valueOf(attendance.workedDays() + attendance.paidHolidayDays())
                .divide(BigDecimal.valueOf(payableDays), 8, RoundingMode.HALF_UP)
                .max(MONEY_ZERO).min(BigDecimal.ONE);
    }

    private BigDecimal hourlyRate(BigDecimal monthlyBase, int standardMinutes) {
        return monthlyBase.divide(BigDecimal.valueOf(standardMinutes), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal overtimeTaxExempt(BigDecimal monthlyBase, int standardMinutes,
                                         int regularMinutes, int restMinutes, int holidayMinutes,
                                         BigDecimal overtimePay, LocalDate periodStart) {
        if (periodStart.getYear() >= 2026) {
            return overtimePay;
        }
        if (standardMinutes <= 0) {
            return MONEY_ZERO;
        }
        BigDecimal perMinute = hourlyRate(monthlyBase, standardMinutes);
        return money(perMinute.multiply(BigDecimal.valueOf(regularMinutes)).multiply(new BigDecimal("0.5"))
                .add(perMinute.multiply(BigDecimal.valueOf(restMinutes)))
                .add(perMinute.multiply(BigDecimal.valueOf(holidayMinutes)).multiply(new BigDecimal("2"))));
    }

    private BigDecimal cappedInsuranceBase(BigDecimal earnedBase, LocalDate periodStart) {
        BigDecimal referenceSalary = periodStart.isBefore(LocalDate.of(2026, 7, 1))
                ? new BigDecimal("2340000") : new BigDecimal("2530000");
        BigDecimal maximumBase = referenceSalary.multiply(BigDecimal.valueOf(20));
        return earnedBase.min(maximumBase).max(MONEY_ZERO);
    }

    private BigDecimal cappedUnemploymentBase(BigDecimal salaryBase, LocalDate periodStart) {
        BigDecimal regionalMinimum = periodStart.getYear() >= 2026
                ? unemploymentRegionMinimum2026
                : periodStart.getYear() >= 2024 ? unemploymentRegionMinimum2024To2025 : new BigDecimal("4680000");
        return salaryBase.min(regionalMinimum.multiply(BigDecimal.valueOf(20))).max(MONEY_ZERO);
    }

    private BigDecimal progressiveTax(BigDecimal base, int year) {
        if (year >= 2026) {
            return sumTax(base,
                    new BigDecimal("10000000"), new BigDecimal("30000000"),
                    new BigDecimal("60000000"), new BigDecimal("100000000"),
                    new BigDecimal("0.05"), new BigDecimal("0.10"),
                    new BigDecimal("0.20"), new BigDecimal("0.30"), new BigDecimal("0.35"));
        }
        return sumTax(base,
                new BigDecimal("5000000"), new BigDecimal("10000000"),
                new BigDecimal("18000000"), new BigDecimal("32000000"),
                new BigDecimal("52000000"), new BigDecimal("80000000"),
                new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.15"),
                new BigDecimal("0.20"), new BigDecimal("0.25"), new BigDecimal("0.30"),
                new BigDecimal("0.35"));
    }

    private BigDecimal sumTax(BigDecimal base, BigDecimal... brackets) {
        int rateStart = brackets.length == 9 ? 4 : 6;
        BigDecimal result = MONEY_ZERO;
        BigDecimal previous = MONEY_ZERO;
        for (int i = 0; i < rateStart; i++) {
            BigDecimal ceiling = brackets[i];
            BigDecimal amount = base.min(ceiling).subtract(previous).max(MONEY_ZERO);
            result = result.add(amount.multiply(brackets[rateStart + i]));
            previous = ceiling;
        }
        result = result.add(base.subtract(previous).max(MONEY_ZERO).multiply(brackets[brackets.length - 1]));
        return money(result);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).multiply(VND);
    }

    public record Result(
            BigDecimal baseSalary,
            BigDecimal totalAllowance,
            BigDecimal overtimePay,
            BigDecimal grossSalary,
            BigDecimal socialInsurance,
            BigDecimal healthInsurance,
            BigDecimal unemploymentInsurance,
            BigDecimal pitDeduction,
            BigDecimal netSalary,
            BigDecimal employerSocialInsurance,
            BigDecimal employerHealthInsurance,
            BigDecimal employerUnemploymentInsurance,
            BigDecimal employerCost,
            BigDecimal insuranceBase,
            BigDecimal unemploymentInsuranceBase,
            BigDecimal taxableIncome,
            BigDecimal taxBase,
            BigDecimal overtimeHours,
            BigDecimal paidRatio,
            BigDecimal overtimeTaxExempt
    ) {
    }
}
