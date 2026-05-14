package com.nailed.web.order.entity;

import com.nailed.common.entity.BaseEntity;
import com.nailed.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 주문 엔티티
// 거래 흐름: REQUESTED → PAID → SHIPPING → DELIVERED → COMPLETED
//           REQUESTED/PAID → CANCEL → CANCELLED
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // 취소 거절 시 이전 상태로 복원하기 위해 저장
    @Enumerated(EnumType.STRING)
    private OrderStatus previousStatus;

    @Column(nullable = false)
    private Integer totalPrice;

    private String cancelReason;

    // 각 단계별 처리 시각 (null = 아직 미도달)
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    public void pay() {
        this.previousStatus = this.status;
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void ship() {
        this.previousStatus = this.status;
        this.status    = OrderStatus.SHIPPING;
        this.shippedAt = LocalDateTime.now();
    }

    public void deliver() {
        this.previousStatus = this.status;
        this.status      = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void complete() {
        this.previousStatus = this.status;
        this.status      = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 배송 시작 후에는 취소 불가
    public void requestCancel(String reason) {
        if (this.status == OrderStatus.SHIPPING
                || this.status == OrderStatus.DELIVERED
                || this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("배송 시작 후에는 취소할 수 없습니다.");
        }
        this.previousStatus = this.status;
        this.status         = OrderStatus.CANCEL;
        this.cancelReason   = reason;
    }

    public void confirmCancel() {
        this.status      = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 취소 거절 시 이전 상태로 복원
    public void rejectCancel() {
        this.status         = this.previousStatus;
        this.previousStatus = null;
        this.cancelReason   = null;
    }

    // 결제 실패 시 주문 초기화
    public void rollbackToRequested() {
        this.status = OrderStatus.REQUESTED;
        this.paidAt = null;
    }

    public boolean isBuyer(Long memberId)  { return this.buyerId.equals(memberId); }
    public boolean isSeller(Long memberId) { return this.sellerId.equals(memberId); }
    public boolean isParty(Long memberId)  { return isBuyer(memberId) || isSeller(memberId); }
}
