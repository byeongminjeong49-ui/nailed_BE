package com.nailed.domain.penalty.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import com.nailed.domain.penalty.dto.PenaltyRequest;
import com.nailed.domain.penalty.dto.PenaltyResponse;
import com.nailed.domain.penalty.entity.MemberPenalty;
import com.nailed.domain.penalty.repository.MemberPenaltyRepository;
import com.nailed.domain.report.entity.Report;
import com.nailed.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PenaltyService {

    private final MemberPenaltyRepository penaltyRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public Long impose(PenaltyRequest.Impose request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Report report = null;
        if (request.reportId() != null) {
            report = reportRepository.findById(request.reportId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
        }

        LocalDateTime startsAt = LocalDateTime.now();
        LocalDateTime endsAt = null;

        if ("SUSPEND".equals(request.penaltyType()) && request.penaltyDays() != null) {
            endsAt = startsAt.plusDays(request.penaltyDays());
            member.lock(endsAt);
        } else if ("BAN".equals(request.penaltyType())) {
            member.lock(LocalDateTime.of(9999, 12, 31, 0, 0));
        }

        MemberPenalty penalty = MemberPenalty.builder()
                .member(member)
                .penaltyType(request.penaltyType())
                .penaltyDays(request.penaltyDays())
                .reason(request.reason())
                .report(report)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .build();

        return penaltyRepository.save(penalty).getPenaltyId();
    }

    public Page<PenaltyResponse.Detail> getMemberPenalties(String memberId, Pageable pageable) {
        return penaltyRepository.findByMemberMemberId(memberId, pageable)
                .map(PenaltyResponse.Detail::from);
    }
}
