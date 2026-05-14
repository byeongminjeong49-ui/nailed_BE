package com.nailed.web.payment.entity;

import com.nailed.common.entity.BaseEntity;
import com.nailed.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 1건에 결제 1건 (중복 결제 방지)
    @Column(nullable = false, unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer amount;

    private String kakaoPgToken; // 결제 승인 토큰
    private String kakaoTid;     // 카카오페이 거래 번호

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    // 결제 승인 완료
    public void complete(String kakaoTid, String pgToken) {
        this.status       = PaymentStatus.COMPLETED;
        this.kakaoTid     = kakaoTid;
        this.kakaoPgToken = pgToken;
        this.paidAt       = LocalDateTime.now();
    }

    // 결제 실패
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    // 환불 처리
    public void refund() {
        this.status     = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

} //class
