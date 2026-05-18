package com.nailed.web.review.dto;

import com.nailed.common.response.PageResponse;
import com.nailed.web.review.entity.Review;

import java.time.LocalDateTime;

public class ReviewResponse {

    /** 리뷰 단건 상세 */
    public record Detail(
            Long reviewId,
            String orderId,
            String buyerNickname,   // 작성자 닉네임
            int rating,
            String content,
            LocalDateTime createdAt
    ) {
        public static Detail from(Review review) {
            return new Detail(
                    review.getReviewId(),
                    review.getOrder().getOrderId(),
                    review.getBuyer().getNickname(),
                    review.getRating(),
                    review.getContent(),
                    review.getCreatedAt()
            );
        }
    }

    /** 판매자 리뷰 목록 (평균 별점 + 페이지) */
    public record SellerReviews(
            Double averageRating,           // 리뷰 없으면 null
            PageResponse<Detail> reviews
    ) {}
}
