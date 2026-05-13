package com.nailed.web.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

	private Long    id;
	private Long    productId;
	private Long    buyerId;
	private Long    sellerId;
	private Integer totalPrice;
	private String  status;        // REQUESTED / PAID / SHIPPING / DELIVERED / PURCHASE_CONFIRMED / CANCEL_REQUESTED / CANCELLED / PAYMENT_FAILED
	private String  statusLabel;   // 한글 상태명
	private String  courier;       // 택배사
	private String  trackingNumber; // 운송장 번호
	private LocalDateTime createdAt;
	private LocalDateTime paidAt;
	private LocalDateTime shippedAt;
	private LocalDateTime deliveredAt;
	private LocalDateTime confirmedAt;

}//class
