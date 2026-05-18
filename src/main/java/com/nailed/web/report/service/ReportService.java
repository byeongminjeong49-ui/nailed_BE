package com.nailed.web.report.service;

import com.nailed.common.enums.ReportReason;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.util.EnumUtil;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.report.dto.ReportRequest;
import com.nailed.web.report.dto.ReportResponse;
import com.nailed.web.report.entity.Report;
import com.nailed.web.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    /**
     * 신고 접수
     * - 본인 신고 불가 (reporter == target 검증)
     * - 중복 신고는 IA 기준 허용 (동일 대상 재신고 가능)
     * - report_id: RPT_001 형태로 생성
     */
    @Transactional
    public ReportResponse.Detail submit(String reporterId, ReportRequest.Submit req) {
        // 본인 신고 불가
        if (reporterId.equals(req.targetMemberId())) {
            throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        Member reporter = findMember(reporterId);
        Member targetMember = findMember(req.targetMemberId());

        // 신고 사유 코드 검증
        ReportReason reasonCode = EnumUtil.parse(ReportReason.class, req.reasonCode(), ErrorCode.INVALID_REPORT_REASON);

        // RPT_XXX 형태 ID 생성
        String reportId = generateReportId();

        Report report = Report.builder()
                .reportId(reportId)
                .reporter(reporter)
                .targetMember(targetMember)
                .reasonCode(reasonCode)
                .detail(req.detail())
                .build();

        return ReportResponse.Detail.from(reportRepository.save(report));
    }

    // ── 내부 유틸 ────────────────────────────────────────────

    private Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 현재 최대 시퀀스 + 1로 RPT_XXX ID 생성
     * 예: RPT_001 / RPT_002 / RPT_010
     * 주의: 동시 접수 시 중복 가능성 있음 → 운영 환경에서는 DB 시퀀스 or UUID 전환 권장
     */
    private String generateReportId() {
        long nextSeq = reportRepository.findMaxSequence() + 1;
        return String.format("RPT_%03d", nextSeq);
    }
}
