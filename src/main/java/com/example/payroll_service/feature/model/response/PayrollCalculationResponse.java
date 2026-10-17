package com.example.payroll_service.feature.model.response;

import java.util.List;

public record PayrollCalculationResponse(
        PayrollRunResponse run,
        List<String> warnings
) {
}
