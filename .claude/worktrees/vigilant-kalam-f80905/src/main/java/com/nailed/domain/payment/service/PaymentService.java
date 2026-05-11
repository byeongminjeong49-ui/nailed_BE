package com.nailed.domain.payment.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.order.entity.Order;
import com.nailed.domain.order.repository.OrderRepository;
import com.nailed.domain.payment.dto.PaymentRequest;
import com.nailed.domain.payment.dto.PaymentResponse;
import com.nailed.domain.payment.entity.Payment;
import com.nailed.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final AtomicLong sequence = new AtomicLong(1);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public String ready(PaymentRequest.Ready request) {
        if (paymentRepository.existsByOrderOrderId(request.orderId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        String paymentId = "PAY_" + String.format("%03d", sequence.getAndIncrement());

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .order(order)
                .method(request.method())
                .amount(order.getFinalPrice())
                .build();

        return paymentRepository.save(payment).getPaymentId();
    }

    @Transactional
    public PaymentResponse.Detail approve(PaymentRequest.Approve request) {
        Payment payment = paymentRepository.findByOrderOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.approve(request.pgToken());
        payment.getOrder().pay();

        return PaymentResponse.Detail.from(payment);
    }

    public PaymentResponse.Detail getByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.Detail.from(payment);
    }
}
