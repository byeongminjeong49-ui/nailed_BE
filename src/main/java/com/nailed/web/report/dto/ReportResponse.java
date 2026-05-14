package com.nailed.web.report.dto;

import com.nailed.web.report.entity.Report;

import java.time.LocalDateTime;

public class ReportResponse {

    public record Item(
            Long reportId,
            Long reporterId,
            Long targetId,
            String targetType,
            String reason,
            String content,
            String status,
            String adminMemo,
            LocalDateTime createdAt
    ) {
        public static Item from(Report r) {
            return new Item(
                    r.getReportId(), r.getReporterId(), r.getTargetId(),
                    r.getTargetType().name(), r.getReason().name(),
                    r.getContent(), r.getStatus().name(),
                    r.getAdminMemo(), r.getCreatedAt()
            );
        }
    }
}
