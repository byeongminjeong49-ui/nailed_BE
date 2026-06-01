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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {
    private final OrderService orderService;
    private final ShippingService shippingService;
    // POST /api/orders?buyerId=xxx&sellerId=yyy
    @PostMapping("")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestParam("buyerId") String buyerId,
            @RequestParam("sellerId") String sellerId,
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
    // PATCH /api/orders/{orderId}/pay — 결제 처리 (mock)
    @PatchMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponseDto> mockPay(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(orderService.mockPay(orderId));
    }
    // PATCH /api/orders/{orderId}/confirm — 주문 확인 (판매자)
    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable String orderId,
            @RequestParam String sellerId) {
        return ResponseEntity.ok(orderService.confirmOrder(orderId, sellerId));
    }
    // POST /api/orders/{orderId}/cancel
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable("orderId") String orderId,
            @RequestParam("buyerId") String buyerId
    ) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, buyerId));
    }
}