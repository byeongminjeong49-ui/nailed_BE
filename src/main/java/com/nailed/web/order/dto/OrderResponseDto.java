package com.nailed.web.order.dto;

import com.nailed.web.order.entity.Order;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponseDto {

	private String orderId;
	private Long productId;
	private String buyerId;
	private String sellerId;

	private Integer commission;
	private Integer productPrice;
	private Integer shippingFee;
	private Integer finalPrice;
	private Integer sellerSettlementAmount;

	private String receiverName;
	private String receiverPhone;
	private String receiverZipcode;
	private String receiverAddress;
	private String receiverAddressDetail;
	private String deliveryRequest;

	private String orderStatus;
	private String previousStatus;

	private String cancelRequestStatus;
	private LocalDateTime cancelRequestedAt;
	private String cancelRequestReason;
	private LocalDateTime cancelRespondedAt;

	private String carrierCode;
	private String trackingNumber;

	private LocalDateTime updatedAt;
	private LocalDateTime paidAt;
	private LocalDateTime requestedAt;
	private LocalDateTime shippedAt;
	private LocalDateTime deliveredAt;
	private LocalDateTime cancelledAt;

    public static OrderResponseDto from(Order order, int shippingFee, int productPrice) {
        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .productId(order.getProductId())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .commission(order.getCommission())
                .productPrice(productPrice)
                .shippingFee(shippingFee)
                .finalPrice(order.getFinalPrice())
                .sellerSettlementAmount(order.getSellerSettlementAmount())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverZipcode(order.getReceiverZipcode())
                .receiverAddress(order.getReceiverAddress())
                .receiverAddressDetail(order.getReceiverAddressDetail())
                .deliveryRequest(order.getDeliveryRequest())
                .orderStatus(order.getOrderStatus())
                .previousStatus(order.getPreviousStatus())
                .cancelRequestStatus(order.getCancelRequestStatus())
                .cancelRequestedAt(order.getCancelRequestedAt())
                .cancelRequestReason(order.getCancelRequestReason())
                .cancelRespondedAt(order.getCancelRespondedAt())
                .carrierCode(order.getCarrierCode())
                .trackingNumber(order.getTrackingNumber())
                .updatedAt(order.getUpdatedAt())
                .paidAt(order.getPaidAt())
                .requestedAt(order.getRequestedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }
}
