package com.nailed.domain.review.dto;

import com.nailed.domain.review.entity.Review;

import java.time.LocalDateTime;

public class ReviewResponse {

    public record Detail(
            Long reviewId,
            String orderId,
            String buyerId,
            String buyerNickname,
            int rating,
            String content,
            LocalDateTime createdAt
    ) {
        public static Detail from(Review review) {
            return new Detail(
                    review.getReviewId(),
                    review.getOrder().getOrderId(),
                    review.getBuyer().getMemberId(),
                    review.getBuyer().getNickname(),
                    review.getRating(),
                    review.getContent(),
                    review.getCreatedAt()
            );
        }
    }
}
