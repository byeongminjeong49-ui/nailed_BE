package com.nailed.web.payment.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.payment.dto.response.KakaoPayReadyResponse;
import com.nailed.web.payment.dto.response.PaymentResponse;
import com.nailed.web.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 1. 결제 준비 - 카카오페이 결제창 URL 반환
    @PostMapping("/api/orders/{orderId}/pay")
    public ResponseEntity<ApiResponse<KakaoPayReadyResponse>> readyPayment(@PathVariable Long orderId) {
        Long buyerId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.readyPayment(orderId, buyerId)));
    }

    // 2. 결제 승인 - 카카오페이가 결제 완료 후 redirect 로 호출
    @GetMapping("/api/payments/kakao/approve")
    public ResponseEntity<ApiResponse<PaymentResponse>> approvePayment(
            @RequestParam Long orderId,
            @RequestParam("pg_token") String pgToken,
            @RequestParam String tid) {
        Long buyerId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.approvePayment(orderId, buyerId, pgToken, tid)));
    }

    // 3. 결제창에서 취소 버튼 클릭 (주문 상태 유지)
    @GetMapping("/api/payments/kakao/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(@RequestParam Long orderId) {
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 4. 결제 실패 콜백
    @GetMapping("/api/payments/kakao/fail")
    public ResponseEntity<ApiResponse<Void>> failPayment(@RequestParam Long orderId) {
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 5. 결제 정보 조회
    @GetMapping("/api/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long orderId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPayment(orderId, memberId)));
    }

} //class
