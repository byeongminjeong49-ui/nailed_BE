package com.nailed.web.order.service;

import com.nailed.web.order.dto.OrderDTO;
import com.nailed.web.order.dto.OrderHistoryDTO;
import com.nailed.web.order.dto.TrackingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

	//** 주문 생성 (즉시구매) - 비관적 락으로 동시 구매 방지
	OrderDTO createOrder(Long productId, Long buyerId, Long sellerId, Integer price);

	//** 주문 단건 조회
	OrderDTO getOrder(Long orderId, Long memberId);

	//** 거래 내역 (구매/판매 통합) - GET /api/members/me/orders?type=BUY or SELL
	Page<OrderDTO> getMyOrders(Long memberId, String type, String status, Pageable pageable);

	//** 배송 추적 (Mock) - 운송장 등록 시각 기준 경과 시간으로 단계 계산
	TrackingDTO getTracking(Long orderId, Long memberId);

	//** 거래 상태 이력 - orders 단일 테이블 기반 (별도 이력 테이블 없음)
	List<OrderHistoryDTO> getOrderHistory(Long orderId, Long memberId);

	//** 운송장 입력 (판매자) → 상태: PAID → SHIPPING
	void enterShipping(Long orderId, Long sellerId, String courier, String trackingNumber);

	//** 배송 완료 처리 (Mock 배송 추적 연동) → 상태: SHIPPING → DELIVERED
	void completeDelivery(Long orderId);

	//** 구매 확정 (구매자) → 상태: DELIVERED → PURCHASE_CONFIRMED + 정산 생성
	void confirmPurchase(Long orderId, Long buyerId);

	//** 취소 요청 (구매자) → 배송 시작 전(PAID)에만 가능
	void requestCancel(Long orderId, Long buyerId);

	//** 취소 수락 (판매자) → 상태: CANCEL_REQUESTED → CANCELLED + 환불 + 상품 복원
	void acceptCancel(Long orderId, Long sellerId);

	//** 취소 거절 (판매자) → 상태: CANCEL_REQUESTED → PAID (원상 복원)
	void rejectCancel(Long orderId, Long sellerId);

}//interface
