package com.nailed.domain.order.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.order.dto.OrderRequest;
import com.nailed.domain.order.dto.OrderResponse;
import com.nailed.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> placeOrder(
            @RequestParam String buyerId,
            @Valid @RequestBody OrderRequest.Place request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.placeOrder(buyerId, request)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse.Detail>> getDetail(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getDetail(orderId)));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse.Detail>>> getBuyerOrders(
            @PathVariable String buyerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getBuyerOrders(buyerId, pageable)));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse.Detail>>> getSellerOrders(
            @PathVariable String sellerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getSellerOrders(sellerId, pageable)));
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<Void>> ship(
            @PathVariable String orderId,
            @Valid @RequestBody OrderRequest.Ship request) {
        orderService.ship(orderId, request);
        return ResponseEntity.ok(ApiResponse.ok("배송 처리되었습니다.", null));
    }

    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(@PathVariable String orderId) {
        orderService.complete(orderId);
        return ResponseEntity.ok(ApiResponse.ok("구매 확정되었습니다.", null));
    }

    @PostMapping("/{orderId}/cancel-request")
    public ResponseEntity<ApiResponse<Void>> requestCancel(
            @PathVariable String orderId,
            @RequestBody OrderRequest.CancelRequest request) {
        orderService.requestCancel(orderId, request);
        return ResponseEntity.ok(ApiResponse.ok("취소 요청이 접수되었습니다.", null));
    }

    @PatchMapping("/{orderId}/cancel-response")
    public ResponseEntity<ApiResponse<Void>> respondCancel(
            @PathVariable String orderId,
            @RequestBody OrderRequest.CancelResponse request) {
        orderService.respondCancel(orderId, request);
        return ResponseEntity.ok(ApiResponse.ok("취소 요청이 처리되었습니다.", null));
    }
}
