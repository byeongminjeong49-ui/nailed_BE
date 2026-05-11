package com.nailed.domain.product.dto;

import com.nailed.domain.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {

    public record Detail(
            Long productId,
            String sellerId,
            String sellerNickname,
            Long categoryId,
            String categoryName,
            Long brandId,
            String brandName,
            String title,
            int price,
            String description,
            String conditionCode,
            String shippingMethod,
            String size,
            String status,
            String hashtags,
            int viewCount,
            int wishlistCount,
            List<String> imageUrls,
            LocalDateTime createdAt
    ) {
        public static Detail from(Product product) {
            return new Detail(
                    product.getProductId(),
                    product.getSeller().getMemberId(),
                    product.getSeller().getNickname(),
                    product.getCategory().getGroupId(),
                    product.getCategory().getName(),
                    product.getBrand() != null ? product.getBrand().getGroupId() : null,
                    product.getBrand() != null ? product.getBrand().getName() : null,
                    product.getTitle(),
                    product.getPrice(),
                    product.getDescription(),
                    product.getConditionCode(),
                    product.getShippingMethod(),
                    product.getSize(),
                    product.getStatus(),
                    product.getHashtags(),
                    product.getViewCount(),
                    product.getWishlistCount(),
                    product.getImages().stream().map(img -> img.getImageUrl()).toList(),
                    product.getCreatedAt()
            );
        }
    }

    public record Summary(
            Long productId,
            String title,
            int price,
            String status,
            int wishlistCount,
            String thumbnailUrl,
            LocalDateTime createdAt
    ) {
        public static Summary from(Product product) {
            String thumbnail = product.getImages().isEmpty()
                    ? null
                    : product.getImages().get(0).getImageUrl();
            return new Summary(
                    product.getProductId(),
                    product.getTitle(),
                    product.getPrice(),
                    product.getStatus(),
                    product.getWishlistCount(),
                    thumbnail,
                    product.getCreatedAt()
            );
        }
    }
}
