package com.nailed.web.payment.dto.response;

import com.nailed.common.enums.PaymentStatus;
import com.nailed.web.payment.entity.Payment;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PaymentResponse {

    private final Long          id;
    private final Long          orderId;
    private final PaymentStatus status;
    private final Integer       amount;
    private final LocalDateTime paidAt;

    public PaymentResponse(Payment payment) {
        this.id      = payment.getId();
        this.orderId = payment.getOrderId();
        this.status  = payment.getStatus();
        this.amount  = payment.getAmount();
        this.paidAt  = payment.getPaidAt();
    }

} //class
