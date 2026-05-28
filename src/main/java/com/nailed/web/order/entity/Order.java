package com.nailed.web.order.entity;
import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
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
    @Column(name = "order_status", length = 20)
    private String orderStatus;
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
    private String cancelRequestStatus;
    private LocalDateTime cancelRequestedAt;
    private String cancelRequestReason;
    private LocalDateTime cancelRespondedAt;
    private String carrierCode;
    private String trackingNumber;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    public void changeStatus(String newStatus) {
        System.out.println("changeStatus 호출: " + this.orderStatus + " → " + newStatus);
        this.previousStatus = this.orderStatus;
        this.orderStatus = newStatus;
        System.out.println("변경 후: " + this.orderStatus);
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