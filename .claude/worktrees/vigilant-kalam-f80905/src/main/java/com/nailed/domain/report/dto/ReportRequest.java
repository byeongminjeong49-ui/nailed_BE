package com.nailed.domain.report.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportRequest {

    public record Submit(
            @NotBlank String targetType,
            @NotBlank String reasonCode,
            String detail,
            String targetMemberId,
            Long targetProductId
    ) {}

    public record Process(
            @NotBlank String action,
            String reason
    ) {}
}
