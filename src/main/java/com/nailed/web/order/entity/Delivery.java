package com.nailed.web.order.entity;

import com.nailed.common.entity.BaseEntity;
import com.nailed.common.enums.CourierCode;
import com.nailed.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 배송(운송장) 엔티티
// registeredAt 기준: 2분→배송중, 5분→간선하차, 10분→배송완료 (Mock 시뮬레이션)
@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierCode courierCode;

    @Column(nullable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    // Mock 배송 단계 계산 기준 시각
    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Builder
    public Delivery(Long orderId, CourierCode courierCode, String trackingNumber) {
        this.orderId        = orderId;
        this.courierCode    = courierCode;
        this.trackingNumber = trackingNumber;
        this.status         = DeliveryStatus.READY;
        this.registeredAt   = LocalDateTime.now();
    }
}
