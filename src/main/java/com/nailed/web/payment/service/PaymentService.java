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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository  paymentRepository;
    private final OrderRepository    orderRepository;
    private final KakaoPayClient     kakaoPayClient;
    private final ProductCommandPort productCommandPort;

    // ── 1. 결제 준비: 카카오페이 결제창 URL 반환 (IA nld-602) ────────────────────
    @Transactional
    public KakaoPayReadyResponse readyPayment(Long orderId, Long buyerId) {

        Order order = findOrderAsBuyer(orderId, buyerId);

        // 이미 완료된 결제가 있으면 중복 결제 차단 (IA nld-602 명세)
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

            // 결제 금액 검증
            if (!result.getAmount().getTotal().equals(order.getTotalPrice())) {
                throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }

            payment.complete(result.getTid(), pgToken);
            order.pay();

            return new PaymentResponse(payment);

        } catch (Exception e) {
            // 실패 시 결제·주문·상품 상태 원자 롤백 (IA nld-602 트랜잭션 원자성 보장)
            payment.fail();
            order.rollbackToRequested();
            productCommandPort.restoreToOnSale(order.getProductId());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 환불 처리 - 취소 수락 시 OrderService 에서 호출 (IA nld-606 명세)
     *
     * 카카오페이 /v1/payment/cancel API 실제 호출.
     * 환불 실패 시 PAYMENT_REFUND_FAILED 예외 → 호출부(OrderService)에서 처리.
     */
    @Transactional
    public void refundPayment(Long orderId) {
        Payment payment = findPaymentByOrderId(orderId);
        // 카카오페이 실제 환불 API 호출 (tid + 전액 환불)
        kakaoPayClient.cancel(payment.getKakaoTid(), payment.getAmount());
        payment.refund();
    }

    // ── 4. 결제 정보 조회 (구매자·판매자 모두 가능) ───────────────────────────────
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
