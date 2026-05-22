package com.nailed.web.product.dto;

import com.nailed.web.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {

    /** 상품 목록 카드 (썸네일·상품명·가격·상태등급·찜수) */
    public record Summary(
            Long productId,
            String title,
            int price,
            String conditionCode,       // S / A / B / C / D
            String conditionLabel,      // 새제품 / 거의새것 / 상태좋음 ...
            int wishlistCount,
            String thumbnailUrl,        // sort_order=0 이미지 URL (null 가능)
            String productStatus,       // ON_SALE / RESERVED / SOLD
            LocalDateTime createdAt
    ) {
        public static Summary from(Product product, String thumbnailUrl) {
            return new Summary(
                    product.getProductId(),
                    product.getTitle(),
                    product.getPrice(),
                    product.getConditionCode().name(),
                    product.getConditionCode().getLabel(),
                    product.getWishlistCount(),
                    thumbnailUrl,
                    product.getProductStatus().name(),
                    product.getCreatedAt()
            );
        }
    }

    /** 상세 페이지 하단 판매자 프로필 카드 */
    public record SellerInfo(
            String memberId,
            String nickname,
            String sellerGrade,         // BRONZE / SILVER / GOLD / DIAMOND
            long completedOrderCount,   // 거래완료 건수
            Double averageRating        // 평균 평점 (리뷰 없으면 null)
    ) {}

    /** 상세 페이지 전체 데이터 */
    public record Detail(
            Long productId,
            String title,
            int price,
            String conditionCode,
            String conditionLabel,
            String conditionDescription,
            String categoryName,
            String categoryPath,        // 맨즈웨어 > 상의 > 티셔츠
            String brandName,           // 브랜드 없으면 null
            String size,
            String shippingMethod,
            int viewCount,
            int wishlistCount,
            String productStatus,
            String description,
            String hashtags,
            LocalDateTime createdAt,
            List<String> imageUrls,     // sort_order 오름차순
            SellerInfo seller
    ) {
        public static Detail from(Product product, List<String> imageUrls, SellerInfo seller, String categoryPath) {
            return new Detail(
                    product.getProductId(),
                    product.getTitle(),
                    product.getPrice(),
                    product.getConditionCode().name(),
                    product.getConditionCode().getLabel(),
                    product.getConditionCode().getDescription(),
                    product.getCategory().getName(),
                    categoryPath,
                    product.getBrand() != null ? product.getBrand().getName() : null,
                    product.getSize(),
                    product.getShippingMethod(),
                    product.getViewCount(),
                    product.getWishlistCount(),
                    product.getProductStatus().name(),
                    product.getDescription(),
                    product.getHashtags(),
                    product.getCreatedAt(),
                    imageUrls,
                    seller
            );
        }
    }
}
