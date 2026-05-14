package com.nailed.web.report.service;

import com.nailed.common.enums.ReportReason;
import com.nailed.common.enums.ReportStatus;
import com.nailed.common.enums.ReportTargetType;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.util.EnumUtil;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.report.dto.ReportRequest;
import com.nailed.web.report.dto.ReportResponse;
import com.nailed.web.report.entity.Report;
import com.nailed.web.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public Long createReport(ReportRequest.Create request) {
        Long reporterId = SecurityUtil.getCurrentMemberId();
        if (reporterId.equals(request.targetId())) {
            throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        ReportTargetType targetType = EnumUtil.parse(ReportTargetType.class, request.targetType(), ErrorCode.INVALID_INPUT_VALUE);
        ReportReason reason = EnumUtil.parse(ReportReason.class, request.reason(), ErrorCode.INVALID_INPUT_VALUE);

        return reportRepository.save(new Report(reporterId, request.targetId(), targetType, reason, request.content()))
                .getReportId();
    }

    public Page<ReportResponse.Item> getAdminReports(String status, String targetType, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        ReportStatus reportStatus = status != null
                ? EnumUtil.parse(ReportStatus.class, status, ErrorCode.INVALID_INPUT_VALUE) : null;
        ReportTargetType type = targetType != null
                ? EnumUtil.parse(ReportTargetType.class, targetType, ErrorCode.INVALID_INPUT_VALUE) : null;

        return reportRepository.findFiltered(reportStatus, type, pageable)
                .map(ReportResponse.Item::from);
    }

    @Transactional
    public void processReport(Long reportId, ReportRequest.Process request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        ReportStatus newStatus = EnumUtil.parse(ReportStatus.class, request.status(), ErrorCode.INVALID_INPUT_VALUE);
        report.process(newStatus, request.adminMemo());
    }
}
