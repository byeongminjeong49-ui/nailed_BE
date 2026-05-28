package com.nailed.web.order.dto;

import java.time.LocalDateTime;

public class AdminOrderResponse {

    public record Summary(
            String orderId,
            String buyerId,
            String buyerUserid,
            String buyerNickname,
            String sellerId,
            String sellerUserid,
            String sellerNickname,
            Long productId,
            String productTitle,
            String productThumbnailUrl,
            String orderStatus,
            Integer orderAmount,
            Integer paymentAmount,
            LocalDateTime createdAt,
            LocalDateTime completedAt,
            LocalDateTime updatedAt
    ) {}
}
