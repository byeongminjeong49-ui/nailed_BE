package com.nailed.web.order.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @Column(name = "order_id", length = 20)
    private String orderId;
    @Column(name = "order_status", length = 20)
    private String orderStatus;
    private String buyerId;
    private String sellerId;
    private Long productId;
    private Integer commission;
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
    private LocalDateTime requestedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    public void changeStatus(String newStatus) {
        System.out.println("changeStatus 호출: " + this.orderStatus + " → " + newStatus);
        this.previousStatus = this.orderStatus;
        this.orderStatus = newStatus;
        System.out.println("변경 후: " + this.orderStatus);
    }
    // 결제 완료 → paidAt 기록, previousStatus = null (최초 상태 전환)
    public void markAsPaid() {
        changeStatus("PAID");
        this.paidAt = LocalDateTime.now();
    }
    // 구매자 배송 요청 → requestedAt 기록, previousStatus = PAID
    public void markAsRequested() {
        changeStatus("REQUESTED");
        this.requestedAt = LocalDateTime.now();
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
    public void cancelByAdmin(String reason) {
        this.cancelRequestReason = reason;
        cancel();
    }
    public void requestCancel(String reason) {
        this.cancelRequestStatus = "REQUESTED";
        this.cancelRequestReason = reason;
        this.cancelRequestedAt = LocalDateTime.now();
    }
}
