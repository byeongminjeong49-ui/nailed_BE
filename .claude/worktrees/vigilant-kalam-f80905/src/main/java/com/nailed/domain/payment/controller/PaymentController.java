package com.nailed.domain.payment.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.payment.dto.PaymentRequest;
import com.nailed.domain.payment.dto.PaymentResponse;
import com.nailed.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready")
    public ResponseEntity<ApiResponse<String>> ready(@Valid @RequestBody PaymentRequest.Ready request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.ready(request)));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<PaymentResponse.Detail>> approve(
            @Valid @RequestBody PaymentRequest.Approve request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.approve(request)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse.Detail>> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByOrderId(orderId)));
    }
}
