package com.nailed.web.order.controller;

import com.nailed.web.order.dto.OrderDTO;
import com.nailed.web.order.dto.OrderHistoryDTO;
import com.nailed.web.order.dto.ShippingDTO;
import com.nailed.web.order.dto.TrackingDTO;
import com.nailed.web.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

	@Autowired(required = false)
	OrderService orderService;

	//** 주문 생성 - POST /api/orders
	@PostMapping("/api/orders")
	public ResponseEntity<OrderDTO> createOrder(
			@RequestParam Long productId,
			@RequestParam Long buyerId,
			@RequestParam Long sellerId,
			@RequestParam Integer price) {
		OrderDTO result = orderService.createOrder(productId, buyerId, sellerId, price);
		return ResponseEntity.ok(result);
	}

	//** 주문 단건 조회 - GET /api/orders/{orderId}
	@GetMapping("/api/orders/{orderId}")
	public ResponseEntity<OrderDTO> getOrder(
			@PathVariable Long orderId,
			@RequestParam Long memberId) {
		OrderDTO result = orderService.getOrder(orderId, memberId);
		return ResponseEntity.ok(result);
	}

	//** 거래 내역 (구매/판매 통합) - GET /api/members/me/orders?type=BUY or SELL
	@GetMapping("/api/members/me/orders")
	public ResponseEntity<Page<OrderDTO>> getMyOrders(
			@RequestParam Long memberId,
			@RequestParam(defaultValue = "BUY") String type,
			@RequestParam(required = false) String status,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<OrderDTO> result = orderService.getMyOrders(memberId, type, status, pageable);
		return ResponseEntity.ok(result);
	}

	//** 배송 추적 (Mock) - GET /api/orders/{orderId}/tracking
	//=> 운송장 등록 시각 기준 경과 시간으로 단계 자동 계산 (집화완료 → 이동중 → 배달완료)
	@GetMapping("/api/orders/{orderId}/tracking")
	public ResponseEntity<TrackingDTO> getTracking(
			@PathVariable Long orderId,
			@RequestParam Long memberId) {
		TrackingDTO result = orderService.getTracking(orderId, memberId);
		return ResponseEntity.ok(result);
	}

	//** 거래 상태 이력 - GET /api/orders/{orderId}/history
	//=> orders 단일 테이블 기반, 단계별 타임스탬프로 이력 구성
	@GetMapping("/api/orders/{orderId}/history")
	public ResponseEntity<List<OrderHistoryDTO>> getOrderHistory(
			@PathVariable Long orderId,
			@RequestParam Long memberId) {
		List<OrderHistoryDTO> result = orderService.getOrderHistory(orderId, memberId);
		return ResponseEntity.ok(result);
	}

	//** 운송장 입력 (판매자) - POST /api/orders/{orderId}/shipping
	@PostMapping("/api/orders/{orderId}/shipping")
	public ResponseEntity<String> enterShipping(
			@PathVariable Long orderId,
			@RequestParam Long sellerId,
			@RequestBody ShippingDTO shippingDTO) {
		orderService.enterShipping(orderId, sellerId, shippingDTO.getCourier(), shippingDTO.getTrackingNumber());
		return ResponseEntity.ok("운송장이 입력되었습니다.");
	}

	//** 배송 완료 처리 - PATCH /api/orders/{orderId}/delivered
	@PatchMapping("/api/orders/{orderId}/delivered")
	public ResponseEntity<String> completeDelivery(@PathVariable Long orderId) {
		orderService.completeDelivery(orderId);
		return ResponseEntity.ok("배송이 완료되었습니다.");
	}

	//** 구매 확정 - POST /api/orders/{orderId}/confirm
	@PostMapping("/api/orders/{orderId}/confirm")
	public ResponseEntity<String> confirmPurchase(
			@PathVariable Long orderId,
			@RequestParam Long buyerId) {
		orderService.confirmPurchase(orderId, buyerId);
		return ResponseEntity.ok("구매가 확정되었습니다.");
	}

	//** 취소 요청 - POST /api/orders/{orderId}/cancel
	@PostMapping("/api/orders/{orderId}/cancel")
	public ResponseEntity<String> requestCancel(
			@PathVariable Long orderId,
			@RequestParam Long buyerId) {
		orderService.requestCancel(orderId, buyerId);
		return ResponseEntity.ok("취소 요청이 완료되었습니다.");
	}

	//** 취소 수락 - PATCH /api/orders/{orderId}/cancel
	@PatchMapping("/api/orders/{orderId}/cancel")
	public ResponseEntity<String> acceptCancel(
			@PathVariable Long orderId,
			@RequestParam Long sellerId) {
		orderService.acceptCancel(orderId, sellerId);
		return ResponseEntity.ok("취소 수락이 완료되었습니다.");
	}


	//** 취소 거절 (판매자) - DELETE /api/orders/{orderId}/cancel
	@DeleteMapping("/api/orders/{orderId}/cancel")
	public ResponseEntity<String> rejectCancel(
			@PathVariable Long orderId,
			@RequestParam Long sellerId) {
		orderService.rejectCancel(orderId, sellerId);
		return ResponseEntity.ok("취소가 거절되었습니다.");
	}

}//class
