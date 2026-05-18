package com.nailed.web.order.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 엔티티
 * - Review 작성 시 주문 상태 검증 및 구매자 확인에 사용
 * - 주문 생성부터 완료 및 취소까지 전체 라이프사이클 관리
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order extends BaseEntity {

    @Id
    @Column(name = "order_id", length = 20)
    private String orderId;

    @Builder.Default
    private String orderStatus = "REQUESTED";

    private String buyerId;
    private String sellerId;
    private Long productId;

    private Integer commission;
    private Integer productAmount;
    private Integer shippingFee;
    private Integer finalPrice;
    private Integer sellerSettlementAmount;

    private String receiverName;
    private String receiverPhone;
    private String receiverZipcode;
    private String receiverAddress;
    private String receiverAddressDetail;
    private String deliveryRequest;

    private String previousStatus;

    @Builder.Default
    private String cancelRequestStatus = "NONE";

    private LocalDateTime cancelRequestedAt;
    private String cancelRequestReason;
    private LocalDateTime cancelRespondedAt;

    private String carrierCode;
    private String trackingNumber;

    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    public void changeStatus(String newStatus) {
        this.previousStatus = this.orderStatus;
        this.orderStatus = newStatus;
    }

    public void markAsPaid() {
        changeStatus("PAID");
        this.paidAt = LocalDateTime.now();
    }

    public void startShipping(String carrierCode, String trackingNumber) {
        changeStatus("SHIPPING");
        this.carrierCode = carrierCode;
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        changeStatus("DELIVERED");
        this.deliveredAt = LocalDateTime.now();
    }

    public void complete() {
        changeStatus("COMPLETED");
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        changeStatus("CANCELLED");
        this.cancelledAt = LocalDateTime.now();
        this.cancelRequestStatus = "APPROVED";
        this.cancelRespondedAt = LocalDateTime.now();
    }

    public void requestCancel(String reason) {
        this.cancelRequestStatus = "REQUESTED";
        this.cancelRequestReason = reason;
        this.cancelRequestedAt = LocalDateTime.now();
    }
}
