package com.nailed.web.order.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 주문 엔티티 (최소 정의)
 * - Review 작성 시 주문 상태 검증 및 구매자 확인에 사용
 * - 전체 주문 기능은 order 도메인 구현 시 확장 예정
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

    // 주문 상태 (REQUESTED / PAID / SHIPPING / DELIVERED / COMPLETED / CANCELLED)
    @Column(name = "order_status", length = 20, nullable = false)
    @Builder.Default
    private String orderStatus = "REQUESTED";

    // 구매자 회원 ID (Review 작성자 검증에 사용)
    @Column(name = "buyer_id", length = 20, nullable = false)
    private String buyerId;

    // 판매자 회원 ID (판매자별 리뷰 조회에 사용)
    @Column(name = "seller_id", length = 20, nullable = false)
    private String sellerId;
}
