package com.nailed.domain.order.dto;

import com.nailed.domain.order.entity.Order;

import java.time.LocalDateTime;

public class OrderResponse {

    public record Detail(
            String orderId,
            Long productId,
            String productTitle,
            String buyerId,
            String sellerId,
            int productAmount,
            int shippingFee,
            int finalPrice,
            String receiverName,
            String receiverPhone,
            String receiverZipcode,
            String receiverAddress,
            String receiverAddressDetail,
            String status,
            String cancelRequestStatus,
            String carrierCode,
            String trackingNumber,
            LocalDateTime createdAt,
            LocalDateTime paidAt,
            LocalDateTime shippedAt,
            LocalDateTime completedAt
    ) {
        public static Detail from(Order order) {
            return new Detail(
                    order.getOrderId(),
                    order.getProduct().getProductId(),
                    order.getProduct().getTitle(),
                    order.getBuyer().getMemberId(),
                    order.getSeller().getMemberId(),
                    order.getProductAmount(),
                    order.getShippingFee(),
                    order.getFinalPrice(),
                    order.getReceiverName(),
                    order.getReceiverPhone(),
                    order.getReceiverZipcode(),
                    order.getReceiverAddress(),
                    order.getReceiverAddressDetail(),
                    order.getStatus(),
                    order.getCancelRequestStatus(),
                    order.getCarrierCode(),
                    order.getTrackingNumber(),
                    order.getCreatedAt(),
                    order.getPaidAt(),
                    order.getShippedAt(),
                    order.getCompletedAt()
            );
        }
    }
}
