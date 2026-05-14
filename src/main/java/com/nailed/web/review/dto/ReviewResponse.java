package com.nailed.web.review.dto;

import com.nailed.web.review.entity.Review;

import java.time.LocalDateTime;

public class ReviewResponse {

    public record Item(
            Long reviewId,
            Long orderId,
            Long reviewerId,
            Long sellerId,
            int rating,
            String content,
            LocalDateTime createdAt
    ) {
        public static Item from(Review r) {
            return new Item(
                    r.getReviewId(), r.getOrderId(), r.getReviewerId(),
                    r.getSellerId(), r.getRating(), r.getContent(), r.getCreatedAt()
            );
        }
    }
}
