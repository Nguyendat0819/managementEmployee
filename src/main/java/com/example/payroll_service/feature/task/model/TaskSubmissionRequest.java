package com.example.payroll_service.feature.task.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TaskSubmissionRequest(
        @NotBlank @Size(max = 4000) String result,
        @Size(max = 10) List<@NotBlank @Size(max = 2048) @Pattern(regexp = "(?i)^https?://\\S+$") String> evidenceLinks
) {}
