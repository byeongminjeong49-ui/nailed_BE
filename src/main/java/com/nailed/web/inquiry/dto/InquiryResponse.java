package com.nailed.web.inquiry.dto;

import com.nailed.web.inquiry.entity.Inquiry;
import java.time.LocalDateTime;

public class InquiryResponse {

    public record Summary(
            String inquiryId,
            String category,
            String title,
            String inquiryStatus,
            LocalDateTime createdAt,
            LocalDateTime answeredAt
    ) {
        public static Summary from(Inquiry inquiry) {
            return new Summary(
                    inquiry.getInquiryId(),
                    inquiry.getCategory(),
                    inquiry.getTitle(),
                    inquiry.getInquiryStatus().name(),
                    inquiry.getCreatedAt(),
                    inquiry.getAnsweredAt()
            );
        }
    }

    public record Detail(
            String inquiryId,
            String memberId,
            String category,
            String title,
            String content,
            String inquiryStatus,
            String answerContent,
            LocalDateTime createdAt,
            LocalDateTime answeredAt
    ) {
        public static Detail from(Inquiry inquiry) {
            return new Detail(
                    inquiry.getInquiryId(),
                    inquiry.getMember().getMemberId(),
                    inquiry.getCategory(),
                    inquiry.getTitle(),
                    inquiry.getContent(),
                    inquiry.getInquiryStatus().name(),
                    inquiry.getAnswerContent(),
                    inquiry.getCreatedAt(),
                    inquiry.getAnsweredAt()
            );
        }
    }

    public record AdminSummary(
            String inquiryId,
            String memberId,
            String category,
            String title,
            String inquiryStatus,
            LocalDateTime createdAt,
            LocalDateTime answeredAt
    ) {
        public static AdminSummary from(Inquiry inquiry) {
            return new AdminSummary(
                    inquiry.getInquiryId(),
                    inquiry.getMember().getMemberId(),
                    inquiry.getCategory(),
                    inquiry.getTitle(),
                    inquiry.getInquiryStatus().name(),
                    inquiry.getCreatedAt(),
                    inquiry.getAnsweredAt()
            );
        }
    }

    public record AdminDetail(
            String inquiryId,
            String memberId,
            String category,
            String title,
            String content,
            String inquiryStatus,
            String answerContent,
            LocalDateTime createdAt,
            LocalDateTime answeredAt
    ) {
        public static AdminDetail from(Inquiry inquiry) {
            return new AdminDetail(
                    inquiry.getInquiryId(),
                    inquiry.getMember().getMemberId(),
                    inquiry.getCategory(),
                    inquiry.getTitle(),
                    inquiry.getContent(),
                    inquiry.getInquiryStatus().name(),
                    inquiry.getAnswerContent(),
                    inquiry.getCreatedAt(),
                    inquiry.getAnsweredAt()
            );
        }
    }
}
