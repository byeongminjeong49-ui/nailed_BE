package com.nailed.web.product.dto;

import com.nailed.web.product.entity.Product;

import java.time.LocalDateTime;

public class ProductResponse {

    // 상품 상세 화면
    public record Detail(
            Long productId,
            String sellerId,
            String title,
            int price,
            String description,
            String conditionCode,
            String categoryCode,
            String status,
            String imageUrl,
            LocalDateTime createdAt
    ) {
        public static Detail from(Product p) {
            return new Detail(
                    p.getProductId(), p.getSellerId(), p.getTitle(), p.getPrice(),
                    p.getDescription(), p.getConditionCode().name(), p.getCategoryCode().name(),
                    p.getStatus().name(), p.getImageUrl(), p.getCreatedAt()
            );
        }
    }

    // 상품 목록 화면 (요약 정보만)
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
}
