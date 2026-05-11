package com.nailed.domain.report.dto;

import com.nailed.domain.report.entity.Report;

import java.time.LocalDateTime;

public class ReportResponse {

    public record Detail(
            String reportId,
            String reporterId,
            String targetType,
            String reasonCode,
            String detail,
            String targetMemberId,
            Long targetProductId,
            String status,
            String processedReason,
            LocalDateTime processedAt,
            LocalDateTime createdAt
    ) {
        public static Detail from(Report report) {
            return new Detail(
                    report.getReportId(),
                    report.getReporter().getMemberId(),
                    report.getTargetType(),
                    report.getReasonCode(),
                    report.getDetail(),
                    report.getTargetMember() != null ? report.getTargetMember().getMemberId() : null,
                    report.getTargetProduct() != null ? report.getTargetProduct().getProductId() : null,
                    report.getStatus(),
                    report.getProcessedReason(),
                    report.getProcessedAt(),
                    report.getCreatedAt()
            );
        }
    }
}
