package com.nailed.domain.order.entity;

import com.nailed.domain.member.entity.Member;
import com.nailed.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id", length = 20)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @Column(name = "product_amount", nullable = false)
    private int productAmount;

    @Column(name = "shipping_fee", nullable = false)
    @Builder.Default
    private int shippingFee = 0;

    @Column(name = "final_price", nullable = false)
    private int finalPrice;

    @Column(name = "receiver_name", length = 30, nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", length = 50, nullable = false)
    private String receiverPhone;

    @Column(name = "receiver_zipcode", length = 10, nullable = false)
    private String receiverZipcode;

    @Column(name = "receiver_address", length = 200, nullable = false)
    private String receiverAddress;

    @Column(name = "receiver_address_detail", length = 100)
    private String receiverAddressDetail;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "REQUESTED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "cancel_request_status", length = 20, nullable = false)
    @Builder.Default
    private String cancelRequestStatus = "NONE";

    @Column(name = "cancel_requested_at")
    private LocalDateTime cancelRequestedAt;

    @Column(name = "cancel_request_reason", length = 500)
    private String cancelRequestReason;

    @Column(name = "cancel_responded_at")
    private LocalDateTime cancelRespondedAt;

    @Column(name = "carrier_code", length = 20)
    private String carrierCode;

    @Column(name = "tracking_number", length = 30)
    private String trackingNumber;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void pay() {
        this.previousStatus = this.status;
        this.status = "PAID";
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void ship(String carrierCode, String trackingNumber) {
        this.previousStatus = this.status;
        this.status = "SHIPPED";
        this.carrierCode = carrierCode;
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void deliver() {
        this.previousStatus = this.status;
        this.status = "DELIVERED";
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.previousStatus = this.status;
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void requestCancel(String reason) {
        this.previousStatus = this.status;
        this.status = "CANCEL_REQUESTED";
        this.cancelRequestStatus = "REQUESTED";
        this.cancelRequestReason = reason;
        this.cancelRequestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void acceptCancel() {
        this.previousStatus = this.status;
        this.status = "CANCELLED";
        this.cancelRequestStatus = "ACCEPTED";
        this.cancelledAt = LocalDateTime.now();
        this.cancelRespondedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void rejectCancel() {
        this.status = this.previousStatus;
        this.cancelRequestStatus = "REJECTED";
        this.cancelRespondedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
