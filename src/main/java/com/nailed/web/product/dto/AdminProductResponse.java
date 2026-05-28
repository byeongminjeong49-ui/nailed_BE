package com.nailed.web.product.dto;

import java.time.LocalDateTime;

public class AdminProductResponse {

    public record Summary(
            Long productId,
            String title,
            String brandName,
            String categoryName,
            String categoryPath,
            int price,
            String productStatus,
            int viewCount,
            int wishlistCount,
            String sellerId,
            String sellerUserid,
            String sellerNickname,
            String thumbnailUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
