package com.nailed.web.report.dto;

public class ReportRequest {

    public record Create(
            Long targetId,
            String targetType,
            String reason,
            String content
    ) {}

    public record Process(
            String status,
            String adminMemo
    ) {}
}
