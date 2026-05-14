package com.nailed.web.order.entity;

import com.nailed.common.entity.BaseEntity;
import com.nailed.common.enums.CancelRequestStatus;
import jakarta.persistence.*;
import lombok.*;

// 취소 요청 엔티티
// status: null=대기중, REJECTED=거절됨 (승인은 Order.confirmCancel() 로 처리)
@Entity
@Table(name = "order_cancel_requests")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCancelRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    private CancelRequestStatus status;

    public void reject() {
        this.status = CancelRequestStatus.REJECTED;
    }
}
