package com.nailed.web.payment.service;

import com.nailed.common.enums.PaymentStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.order.service.ProductCommandPort;
import com.nailed.web.payment.dto.response.KakaoPayApproveResponse;
import com.nailed.web.payment.dto.response.KakaoPayReadyResponse;
import com.nailed.web.payment.dto.response.PaymentResponse;
import com.nailed.web.payment.entity.Payment;
import com.nailed.web.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository  paymentRepository;
    private final OrderRepository    orderRepository;
    private final KakaoPayClient     kakaoPayClient;
    private final ProductCommandPort productCommandPort;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            KakaoPayClient kakaoPayClient,
            @Qualifier("orderProductCommandPortImpl") ProductCommandPort productCommandPort) {
        this.paymentRepository  = paymentRepository;
        this.orderRepository    = orderRepository;
        this.kakaoPayClient     = kakaoPayClient;
        this.productCommandPort = productCommandPort;
    }

    // ── 1. 결제 준비: 카카오페이 결제창 URL 반환 (IA nld-602) ────────────────────
    @Transactional
    public KakaoPayReadyResponse readyPayment(Long orderId, Long buyerId) {

        Order order = findOrderAsBuyer(orderId, buyerId);

        Optional<Payment> existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent() && existing.get().getStatus() == PaymentStatus.COMPLETED) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .amount(order.getTotalPrice())
                .status(PaymentStatus.PENDING)
                .build());

        return kakaoPayClient.ready(orderId, buyerId, "Nailed 상품", order.getTotalPrice());
    }

    // ── 2. 결제 승인: 카카오페이 결제 완료 후 호출 (IA nld-602) ─────────────────
    @Transactional
    public PaymentResponse approvePayment(Long orderId, Long buyerId, String pgToken, String tid) {

        Order   order   = findOrderAsBuyer(orderId, buyerId);
        Payment payment = findPaymentByOrderId(orderId);

        try {
            KakaoPayApproveResponse result = kakaoPayClient.approve(orderId, buyerId, tid, pgToken);

            if (!result.getAmount().getTotal().equals(order.getTotalPrice())) {
                throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }

            payment.complete(result.getTid(), pgToken);
            order.pay();

            return new PaymentResponse(payment);

        } catch (Exception e) {
            payment.fail();
            order.rollbackToRequested();
            productCommandPort.restoreToOnSale(order.getProductId());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    // ── 3. 환불 처리 (IA nld-606) ────────────────────────────────────────────────
    @Transactional
    public void refundPayment(Long orderId) {
        Payment payment = findPaymentByOrderId(orderId);
        kakaoPayClient.cancel(payment.getKakaoTid(), payment.getAmount());
        payment.refund();
    }

    // ── 4. 결제 정보 조회 ─────────────────────────────────────────────────────────
    public PaymentResponse getPayment(Long orderId, Long memberId) {
        orderRepository.findByIdAndParty(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_UNAUTHORIZED));
        return new PaymentResponse(findPaymentByOrderId(orderId));
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────────

    private Order findOrderAsBuyer(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isBuyer(buyerId)) throw new CustomException(ErrorCode.ORDER_UNAUTHORIZED);
        return order;
    }

    private Payment findPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}