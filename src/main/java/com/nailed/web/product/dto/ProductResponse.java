package com.nailed.web.product.dto;

import com.nailed.web.product.entity.Product;

import java.time.LocalDateTime;

public class ProductResponse {

    public record Detail(
            Long productId,
            Long sellerId,
            String title,
            int price,
            String description,
            String conditionCode,
            String categoryCode,
            String status,
            String imageUrl,
            int viewCount,
            int wishlistCount,
            LocalDateTime createdAt
    ) {
        public static Detail from(Product p) {
            return new Detail(
                    p.getProductId(), p.getSellerId(), p.getTitle(), p.getPrice(),
                    p.getDescription(), p.getConditionCode().name(), p.getCategoryCode().name(),
                    p.getStatus().name(), p.getImageUrl(),
                    p.getViewCount(), p.getWishlistCount(), p.getCreatedAt()
            );
        }
    }

    public record Summary(
            Long productId,
            String title,
            int price,
            String imageUrl,
            LocalDateTime createdAt
    ) {
        public static Summary from(Product p) {
            return new Summary(p.getProductId(), p.getTitle(), p.getPrice(), p.getImageUrl(), p.getCreatedAt());
        }
    }

    public record Card(
            Long productId,
            Long sellerId,
            String title,
            int price,
            String conditionCode,
            String categoryCode,
            String status,
            String imageUrl,
            int viewCount,
            int wishlistCount,
            LocalDateTime createdAt
    ) {
        public static Card from(Product p) {
            return new Card(
                    p.getProductId(), p.getSellerId(), p.getTitle(), p.getPrice(),
                    p.getConditionCode().name(), p.getCategoryCode().name(),
                    p.getStatus().name(), p.getImageUrl(),
                    p.getViewCount(), p.getWishlistCount(), p.getCreatedAt()
            );
        }
    }
}
