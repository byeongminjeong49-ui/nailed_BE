package com.nailed.web.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false)
	private Long buyerId;

	@Column(nullable = false)
	private Long sellerId;

	@Column(nullable = false)
	private Integer totalPrice;

	//=> REQUESTED / PAID / SHIPPING / DELIVERED / PURCHASE_CONFIRMED
	//=> CANCEL_REQUESTED / CANCELLED / PAYMENT_FAILED
	@Column(nullable = false)
	private String status;

	private String courier;        // 택배사
	private String trackingNumber; // 운송장 번호

	// 단계별 타임스탬프 (거래 이력 구성용)
	private LocalDateTime createdAt;
	private LocalDateTime paidAt;       // 결제 완료 시각
	private LocalDateTime shippedAt;    // 배송 시작 시각
	private LocalDateTime deliveredAt;  // 배송 완료 시각
	private LocalDateTime confirmedAt;  // 구매 확정 시각

}//class
