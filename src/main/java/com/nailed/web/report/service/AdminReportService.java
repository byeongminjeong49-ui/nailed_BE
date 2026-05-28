package com.nailed.web.report.service;

import com.nailed.common.enums.ReportReason;
import com.nailed.common.enums.ReportStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.report.dto.AdminReportResponse;
import com.nailed.web.report.entity.Report;
import com.nailed.web.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private static final String TARGET_TYPE_MEMBER = "MEMBER";

    private final ReportRepository reportRepository;

    public PageResponse<AdminReportResponse.Summary> getReports(
            String keyword,
            String targetType,
            String reasonCode,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {
        validateTargetType(targetType);

        var page = reportRepository.searchAdminReports(
                blankToNull(keyword),
                parseReason(reasonCode),
                parseStatus(status),
                dateFrom != null ? dateFrom.atStartOfDay() : null,
                dateTo != null ? dateTo.atTime(LocalTime.MAX) : null,
                pageable
        );

        return PageResponse.of(page.map(this::toSummary));
    }

    private AdminReportResponse.Summary toSummary(Report report) {
        Member reporter = report.getReporter();
        Member target = report.getTargetMember();

        return new AdminReportResponse.Summary(
                report.getReportId(),
                reporter.getMemberId(),
                reporter.getUserid(),
                reporter.getNickname(),
                TARGET_TYPE_MEMBER,
                target.getMemberId(),
                target.getNickname() != null ? target.getNickname() : target.getUserid(),
                null,
                null,
                null,
                report.getReasonCode().name(),
                report.getDetail(),
                report.getReportStatus().name(),
                report.getProcessedReason(),
                report.getProcessedAt(),
                report.getCreatedAt()
        );
    }

    private void validateTargetType(String targetType) {
        String value = blankToNull(targetType);
        if (value == null) {
            return;
        }
        if (!TARGET_TYPE_MEMBER.equals(value.toUpperCase())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ReportReason parseReason(String reasonCode) {
        String value = blankToNull(reasonCode);
        if (value == null) {
            return null;
        }
        try {
            return ReportReason.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REASON);
        }
    }

    private ReportStatus parseStatus(String status) {
        String value = blankToNull(status);
        if (value == null) {
            return null;
        }
        try {
            return ReportStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
