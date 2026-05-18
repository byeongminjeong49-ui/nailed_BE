package com.nailed.web.order.controller;

import com.nailed.web.order.dto.OrderRequestDto;
import com.nailed.web.order.dto.OrderResponseDto;
import com.nailed.web.order.dto.ShippingRequestDto;
import com.nailed.web.order.service.OrderService;
import com.nailed.web.order.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // ⚡ 프론트엔드 포트(5173) 교차 출처 에러(CORS) 방지 및 주소 라우팅 안정화
public class OrderController {

    private final OrderService orderService;
    private final ShippingService shippingService;

    // POST /api/orders?buyerId=xxx&sellerId=yyy
    @PostMapping("") // ⚡ 공백 매핑을 명시하여 /api/orders 진입점의 404 핸들러 충돌 방지
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestParam("buyerId") String buyerId,   // ⚡ 파라미터 이름을 명확히 바인딩
            @RequestParam("sellerId") String sellerId, // ⚡ 파라미터 이름을 명확히 바인딩
            @Valid @RequestBody OrderRequestDto requestDto
    ) {
        OrderResponseDto response = orderService.createOrder(buyerId, sellerId, requestDto);
        return ResponseEntity.created(URI.create("/api/orders/" + response.getOrderId())).body(response);
    }

    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    // GET /api/orders/seller/{sellerId}/count?status=PAID
    @GetMapping("/seller/{sellerId}/count")
    public ResponseEntity<Long> countSellerOrdersByStatus(
            @PathVariable("sellerId") String sellerId,
            @RequestParam("status") String status
    ) {
        return ResponseEntity.ok(orderService.countSellerOrdersByStatus(sellerId, status));
    }

    // PATCH /api/orders/{orderId}/shipping — 운송장 등록 (mock)
    @PatchMapping("/{orderId}/shipping")
    public ResponseEntity<OrderResponseDto> registerTracking(
            @PathVariable("orderId") String orderId,
            @Valid @RequestBody ShippingRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                shippingService.registerTracking(orderId, requestDto.getCarrierCode(), requestDto.getTrackingNumber())
        );
    }

    // PATCH /api/orders/{orderId}/delivered — 배송 완료 처리 (mock)
    @PatchMapping("/{orderId}/delivered")
    public ResponseEntity<OrderResponseDto> confirmDelivery(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(shippingService.confirmDelivery(orderId));
    }
}