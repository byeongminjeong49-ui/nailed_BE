package com.nailed.domain.payment.entity;

import com.nailed.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Payment {

    @Id
    @Column(name = "payment_id", length = 20)
    private String paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "method", length = 30, nullable = false)
    private String method;

    @Column(name = "kakao_tid", length = 50)
    private String kakaoTid;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "READY";

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String kakaoTid) {
        this.kakaoTid = kakaoTid;
        this.status = "PAID";
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = "FAILED";
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = "CANCELLED";
        this.updatedAt = LocalDateTime.now();
    }

    public void refund() {
        this.status = "REFUNDED";
        this.refundedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
