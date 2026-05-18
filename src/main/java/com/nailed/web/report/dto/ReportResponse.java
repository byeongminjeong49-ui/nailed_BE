package com.nailed.web.report.dto;

import com.nailed.web.report.entity.Report;

import java.time.LocalDateTime;

public class ReportResponse {

    /** 신고 접수 결과 */
    public record Detail(
            String reportId,
            String reporterId,
            String targetMemberId,
            String reasonCode,      // FRAUD / ABUSE / PROHIBITED_ITEM / ETC
            String reasonLabel,     // 사기 / 욕설·비방 / 금지상품 / 기타
            String detail,
            String reportStatus,    // PENDING / APPROVED / REJECTED / DONE
            LocalDateTime createdAt
    ) {
        public static Detail from(Report report) {
            return new Detail(
                    report.getReportId(),
                    report.getReporter().getMemberId(),
                    report.getTargetMember().getMemberId(),
                    report.getReasonCode().name(),
                    report.getReasonCode().getLabel(),
                    report.getDetail(),
                    report.getReportStatus().name(),
                    report.getCreatedAt()
            );
        }
    }
}
