package com.nailed.web.member.dto;

import java.time.LocalDateTime;
public class MemberResponse {

    public record Home(
            Profile profile,
            long sellingProductCount,
            long soldProductCount,
            long wishlistCount,
            long buyingOrderCount,
            long sellingOrderCount
    ) {}

    public record Profile(
            String memberId,
            String userid,
            String nickname,
            String name,
            String shopInfo,
            String memberStatus,
            String sellerGrade,
            String role,
            String bankCode,
            String accountNumber,
            String depositorName,
            boolean marketingAgreed,
            LocalDateTime createdAt
    ) {}

    public record ProductSummary(
            Long productId,
            String title,
            int price,
            String conditionCode,
            String productStatus,
            int viewCount,
            int wishlistCount,
            String thumbnailUrl,
            String size,
            String categoryCode,
            String brandName,
            LocalDateTime createdAt
    ) {}

    public record OrderSummary(
            String orderId,
            Long productId,
            String productTitle,
            String thumbnailUrl,
            String buyerId,
            String sellerId,
            int productAmount,
            int shippingFee,
            int finalPrice,
            String orderStatus,
            String cancelRequestStatus,
            LocalDateTime createdAt,
            LocalDateTime paidAt,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime cancelledAt
    ) {}

    public record SettlementSummary(
            String orderId,
            Long productId,
            String productTitle,
            String thumbnailUrl,
            int commission,
            int finalPrice,
            int sellerSettlementAmount,
            String orderStatus,
            LocalDateTime createdAt
    ) {}

}
