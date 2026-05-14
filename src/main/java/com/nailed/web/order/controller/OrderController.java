package com.nailed.web.order.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.common.response.PageResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.order.dto.request.CancelRequest;
import com.nailed.web.order.dto.request.OrderCreateRequest;
import com.nailed.web.order.dto.request.ShippingRequest;
import com.nailed.web.order.dto.response.MyOrderResponse;
import com.nailed.web.order.dto.response.OrderDetailResponse;
import com.nailed.web.order.dto.response.TrackingResponse;
import com.nailed.web.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. 주문 생성
    @PostMapping("/api/orders")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(memberId, request)));
    }

    // 2. 주문 상세 조회
    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(@PathVariable Long orderId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(orderId, memberId)));
    }

    // 3. 거래 타임라인 조회
    @GetMapping("/api/orders/{orderId}/history")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getHistory(@PathVariable Long orderId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getHistory(orderId, memberId)));
    }

    // 4. 운송장 입력 (판매자)
    @PostMapping("/api/orders/{orderId}/shipping")
    public ResponseEntity<ApiResponse<Void>> inputShipping(@PathVariable Long orderId,
                                                           @Valid @RequestBody ShippingRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        orderService.inputShipping(orderId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 5. 배송 추적
    @GetMapping("/api/orders/{orderId}/tracking")
    public ResponseEntity<ApiResponse<TrackingResponse>> getTracking(@PathVariable Long orderId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getTracking(orderId, memberId)));
    }

    // 6. 구매 확정 (구매자)
    @PostMapping("/api/orders/{orderId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPurchase(@PathVariable Long orderId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        orderService.confirmPurchase(orderId, memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 7. 취소 요청
    @PostMapping("/api/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> requestCancel(@PathVariable Long orderId,
                                                           @Valid @RequestBody CancelRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        orderService.requestCancel(orderId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 8. 취소 응답 (accept=true: 승인, false: 거절)
    @PatchMapping("/api/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> respondCancel(@PathVariable Long orderId,
                                                           @RequestParam boolean accept) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        orderService.respondCancel(orderId, memberId, accept);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 9. 마이페이지 거래 내역 (type=BUYER/SELLER, statusGroup=거래중/완료/취소)
    @GetMapping("/api/members/me/orders")
    public ResponseEntity<ApiResponse<PageResponse<MyOrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "BUYER") String type,
            @RequestParam(required = false) String statusGroup,
            @PageableDefault(size = 20) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(orderService.getMyOrders(memberId, type, statusGroup, pageable))));
    }

    // 10. 구매 목록
    @GetMapping("/api/orders/purchases")
    public ResponseEntity<ApiResponse<PageResponse<MyOrderResponse>>> getMyPurchases(
            @PageableDefault(size = 20) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(orderService.getMyOrders(memberId, "BUYER", null, pageable))));
    }

    // 11. 판매 목록
    @GetMapping("/api/orders/sales")
    public ResponseEntity<ApiResponse<PageResponse<MyOrderResponse>>> getMySales(
            @PageableDefault(size = 20) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(orderService.getMyOrders(memberId, "SELLER", null, pageable))));
    }
}
