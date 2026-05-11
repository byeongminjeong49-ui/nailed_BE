package com.nailed.domain.payment.dto;

import com.nailed.domain.payment.entity.Payment;

import java.time.LocalDateTime;

public class PaymentResponse {

    public record Detail(
            String paymentId,
            String orderId,
            String method,
            int amount,
            String status,
            LocalDateTime paidAt,
            LocalDateTime refundedAt,
            LocalDateTime createdAt
    ) {
        public static Detail from(Payment payment) {
            return new Detail(
                    payment.getPaymentId(),
                    payment.getOrder().getOrderId(),
                    payment.getMethod(),
                    payment.getAmount(),
                    payment.getStatus(),
                    payment.getPaidAt(),
                    payment.getRefundedAt(),
                    payment.getCreatedAt()
            );
        }
    }
}
