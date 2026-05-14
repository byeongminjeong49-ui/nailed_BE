package com.nailed.web.review.dto;

public class ReviewRequest {

    public record Write(
            Long orderId,
            Long sellerId,
            int rating,
            String content
    ) {}
}
