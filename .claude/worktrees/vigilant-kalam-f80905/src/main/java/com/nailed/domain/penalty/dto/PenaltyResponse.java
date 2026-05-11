package com.nailed.domain.penalty.dto;

import com.nailed.domain.penalty.entity.MemberPenalty;

import java.time.LocalDateTime;

public class PenaltyResponse {

    public record Detail(
            Long penaltyId,
            String memberId,
            String penaltyType,
            Integer penaltyDays,
            String reason,
            String reportId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            LocalDateTime createdAt
    ) {
        public static Detail from(MemberPenalty penalty) {
            return new Detail(
                    penalty.getPenaltyId(),
                    penalty.getMember().getMemberId(),
                    penalty.getPenaltyType(),
                    penalty.getPenaltyDays(),
                    penalty.getReason(),
                    penalty.getReport() != null ? penalty.getReport().getReportId() : null,
                    penalty.getStartsAt(),
                    penalty.getEndsAt(),
                    penalty.getCreatedAt()
            );
        }
    }
}
