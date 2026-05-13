//package com.nailed.web.order.service;
//
//import com.nailed.web.order.dto.OrderDTO;
//import com.nailed.web.order.dto.OrderHistoryDTO;
//import com.nailed.web.order.dto.TrackingDTO;
//import com.nailed.web.order.dto.TrackingStepDTO;
//import com.nailed.web.order.entity.Order;
//import com.nailed.web.order.repository.OrderRepository;
//import com.nailed.web.payment.service.PaymentService;
//import com.nailed.web.settlement.service.SettlementService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class OrderServiceImpl implements OrderService {
//
//	@Autowired
//	private OrderRepository orderRepository;
//
//	@Autowired(required = false)
//	private PaymentService paymentService;
//
//	@Autowired(required = false)
//	private SettlementService settlementService;
//
//	// IA 보완: 상품 상태 변경을 위해 필요 (임시 Repository 주입 또는 Service 주입)
//	// 실제 환경에 맞춰 ProductRepository나 ProductService 인터페이스가 있다고 가정함
//    // @Autowired
//    // private ProductRepository productRepository; 
//
//	//-----------------------------------------------
//	//** 주문 생성 (즉시구매)
//	@Override
//	@Transactional
//	public OrderDTO createOrder(Long productId, Long buyerId, Long sellerId, Integer price) {
//		Order order = new Order();
//		order.setProductId(productId);
//		order.setBuyerId(buyerId);
//		order.setSellerId(sellerId);
//		order.setTotalPrice(price);
//		order.setStatus("REQUESTED");
//		order.setCreatedAt(LocalDateTime.now());
//
//		orderRepository.save(order);
//		return toDTO(order);
//	}
//
//	//-----------------------------------------------
//	//** 주문 단건 조회 (권한 체크 포함)
//	@Override
//	public OrderDTO getOrder(Long orderId, Long memberId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		// 권한 체크: 구매자 또는 판매자만 조회 가능
//		if (!order.getBuyerId().equals(memberId) && !order.getSellerId().equals(memberId)) {
//			throw new RuntimeException("해당 주문에 대한 조회 권한이 없습니다.");
//		}
//
//		return toDTO(order);
//	}
//
//	//-----------------------------------------------
//	//** 내 주문 목록 조회
//	@Override
//	public Page<OrderDTO> getMyOrders(Long memberId, String type, String status, Pageable pageable) {
//		Page<Order> orders;
//		if ("SELL".equals(type)) {
//			orders = (status == null || status.isEmpty()) 
//					? orderRepository.findBySellerIdOrderByCreatedAtDesc(memberId, pageable)
//					: orderRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(memberId, status, pageable);
//		} else {
//			orders = (status == null || status.isEmpty()) 
//					? orderRepository.findByBuyerIdOrderByCreatedAtDesc(memberId, pageable)
//					: orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(memberId, status, pageable);
//		}
//		return orders.map(this::toDTO);
//	}
//
//	//-----------------------------------------------
//	//** 운송장 입력 (판매자 권한 체크 및 상태 변경)
//	@Override
//	@Transactional
//	public void enterShipping(Long orderId, Long sellerId, String courier, String trackingNumber) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		// 권한 체크: 판매자 본인인지 확인
//		if (!order.getSellerId().equals(sellerId)) {
//			throw new RuntimeException("운송장 입력 권한이 없습니다.");
//		}
//
//		order.setStatus("SHIPPING");
//		order.setCourier(courier);
//		order.setTrackingNumber(trackingNumber);
//		order.setShippedAt(LocalDateTime.now());
//	}
//
//	//-----------------------------------------------
//	//** 배송 완료 처리 (Mock)
//	@Override
//	@Transactional
//	public void completeDelivery(Long orderId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//		order.setStatus("DELIVERED");
//		order.setDeliveredAt(LocalDateTime.now());
//	}
//
//	//-----------------------------------------------
//	//** 구매 확정 (구매자 권한 체크 + 정산 서비스 연동)
//	@Override
//	@Transactional
//	public void confirmPurchase(Long orderId, Long buyerId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		// 권한 체크: 구매자 본인인지 확인
//		if (!order.getBuyerId().equals(buyerId)) {
//			throw new RuntimeException("구매 확정 권한이 없습니다.");
//		}
//
//		order.setStatus("PURCHASE_CONFIRMED");
//		order.setConfirmedAt(LocalDateTime.now());
//
//		// IA 보완: 정산 서비스 연동 (SettlementService 구현체 필요)
//		if (settlementService != null) {
//			settlementService.createSettlement(orderId, order.getSellerId(), order.getTotalPrice());
//		}
//	}
//
//	//-----------------------------------------------
//	//** 취소 요청 (구매자 권한 체크)
//	@Override
//	@Transactional
//	public void requestCancel(Long orderId, Long buyerId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		if (!order.getBuyerId().equals(buyerId)) {
//			throw new RuntimeException("취소 요청 권한이 없습니다.");
//		}
//
//		order.setStatus("CANCEL_REQUESTED");
//	}
//
//	//-----------------------------------------------
//	//** 취소 수락 (판매자 권한 체크 + 상품 상태 복원 연동)
//	@Override
//	@Transactional
//	public void acceptCancel(Long orderId, Long sellerId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		if (!order.getSellerId().equals(sellerId)) {
//			throw new RuntimeException("취소 수락 권한이 없습니다.");
//		}
//
//		order.setStatus("CANCELLED");
//		
//		// IA 보완: 상품 상태 복원 로직 (ProductService 연동 예시)
//		// productService.updateProductStatus(order.getProductId(), "AVAILABLE");
//	}
//
//	//-----------------------------------------------
//	//** 취소 거절 (판매자 권한 체크)
//	@Override
//	@Transactional
//	public void rejectCancel(Long orderId, Long sellerId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		if (!order.getSellerId().equals(sellerId)) {
//			throw new RuntimeException("취소 거절 권한이 없습니다.");
//		}
//
//		// 상태를 이전 상태(결제완료/PAID)로 복구 (비즈니스 정책에 따라 상이)
//		order.setStatus("PAID");
//	}
//
//	//-----------------------------------------------
//	//** 배송 추적 (Mock)
//	@Override
//	public TrackingDTO getTracking(Long orderId, Long memberId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//
//		if (!order.getBuyerId().equals(memberId) && !order.getSellerId().equals(memberId)) {
//			throw new RuntimeException("배송 정보 조회 권한이 없습니다.");
//		}
//
//		TrackingDTO dto = new TrackingDTO();
//		dto.setOrderId(orderId);
//		dto.setCourier(order.getCourier());
//		dto.setTrackingNumber(order.getTrackingNumber());
//		
//		// 시간 기반 Mock 로직 생략 (기존 로직 유지 가능)
//		return dto;
//	}
//
//	//-----------------------------------------------
//	//** 거래 상태 이력 조회
//	@Override
//	public List<OrderHistoryDTO> getOrderHistory(Long orderId, Long memberId) {
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
//		
//		List<OrderHistoryDTO> history = new ArrayList<>();
//		// 각 시각 데이터가 있으면 추가하는 로직...
//		return history;
//	}
//
//	private OrderDTO toDTO(Order order) {
//		OrderDTO dto = new OrderDTO();
//		dto.setId(order.getId());
//		dto.setProductId(order.getProductId());
//		dto.setBuyerId(order.getBuyerId());
//		dto.setSellerId(order.getSellerId());
//		dto.setTotalPrice(order.getTotalPrice());
//		dto.setStatus(order.getStatus());
//		dto.setStatusLabel(getStatusLabel(order.getStatus()));
//		dto.setCourier(order.getCourier());
//		dto.setTrackingNumber(order.getTrackingNumber());
//		dto.setCreatedAt(order.getCreatedAt());
//		dto.setPaidAt(order.getPaidAt());
//		dto.setShippedAt(order.getShippedAt());
//		dto.setDeliveredAt(order.getDeliveredAt());
//		dto.setConfirmedAt(order.getConfirmedAt());
//		return dto;
//	}
//
//	private String getStatusLabel(String status) {
//		if ("REQUESTED".equals(status)) return "결제 대기";
//		if ("PAID".equals(status)) return "결제 완료";
//		if ("SHIPPING".equals(status)) return "배송중";
//		if ("DELIVERED".equals(status)) return "배송 완료";
//		if ("PURCHASE_CONFIRMED".equals(status)) return "구매 확정";
//		if ("CANCEL_REQUESTED".equals(status)) return "취소 요청";
//		if ("CANCELLED".equals(status)) return "취소 완료";
//		return status;
//	}
//}
