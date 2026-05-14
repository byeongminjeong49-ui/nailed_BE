package com.nailed.web.order.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.order.dto.request.CancelRequest;
import com.nailed.web.order.dto.request.OrderCreateRequest;
import com.nailed.web.order.dto.request.ShippingRequest;
import com.nailed.web.order.dto.response.MyOrderResponse;
import com.nailed.web.order.dto.response.OrderDetailResponse;
import com.nailed.web.order.dto.response.TrackingResponse;
import com.nailed.web.order.entity.Delivery;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.entity.OrderCancelRequest;
import com.nailed.web.order.repository.DeliveryRepository;
import com.nailed.web.order.repository.OrderCancelRequestRepository;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.payment.service.PaymentService;
import com.nailed.web.settlement.service.SettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository              orderRepository;
    private final DeliveryRepository           deliveryRepository;
    private final OrderCancelRequestRepository cancelRequestRepository;
    private final SettlementService            settlementService;
    private final PaymentService               paymentService;
    private final ProductCommandPort           productCommandPort;
    private final DeliveryTracker              deliveryTracker;
    private final MemberQueryPort              memberQueryPort;

    public OrderService(
            OrderRepository orderRepository,
            DeliveryRepository deliveryRepository,
            OrderCancelRequestRepository cancelRequestRepository,
            SettlementService settlementService,
            PaymentService paymentService,
            @Qualifier("orderProductCommandPortImpl") ProductCommandPort productCommandPort,
            DeliveryTracker deliveryTracker,
            MemberQueryPort memberQueryPort) {
        this.orderRepository         = orderRepository;
        this.deliveryRepository      = deliveryRepository;
        this.cancelRequestRepository = cancelRequestRepository;
        this.settlementService       = settlementService;
        this.paymentService          = paymentService;
        this.productCommandPort      = productCommandPort;
        this.deliveryTracker         = deliveryTracker;
        this.memberQueryPort         = memberQueryPort;
    }

    // ── 1. 주문 생성 (IA nld-403 비관적 락) ──────────────────────────────────────
    @Transactional
    public OrderDetailResponse createOrder(Long buyerId, OrderCreateRequest request) {

        ProductCommandPort.ProductInfo product;
        try {
            product = productCommandPort.lockAndGetProductInfo(request.getProductId());
        } catch (PessimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.PRODUCT_LOCK_CONFLICT);
        }

        if (product.getSellerId().equals(buyerId))
            throw new CustomException(ErrorCode.SELF_ORDER_NOT_ALLOWED);

        Order order = Order.builder()
                .buyerId(buyerId)
                .sellerId(product.getSellerId())
                .productId(product.getProductId())
                .totalPrice(product.getPrice())
                .status(OrderStatus.REQUESTED)
                .build();
        orderRepository.save(order);
        productCommandPort.markAsSoldOut(product.getProductId());

        return new OrderDetailResponse(order);
    }

    // ── 2. 거래 상세 조회 (IA nld-601) ──────────────────────────────────────────
    public OrderDetailResponse getOrder(Long orderId, Long memberId) {
        return new OrderDetailResponse(findOrderAsParty(orderId, memberId));
    }

    // ── 3. 거래 상태 이력 조회 (IA nld-607) ─────────────────────────────────────
    public OrderDetailResponse getHistory(Long orderId, Long memberId) {
        return new OrderDetailResponse(findOrderAsParty(orderId, memberId));
    }

    // ── 4. 운송장 입력 (IA nld-603) ──────────────────────────────────────────────
    @Transactional
    public void inputShipping(Long orderId, Long sellerId, ShippingRequest request) {

        Order order = findOrder(orderId);

        if (!order.isSeller(sellerId))
            throw new CustomException(ErrorCode.ORDER_UNAUTHORIZED);

        if (deliveryRepository.existsByOrderId(orderId))
            throw new CustomException(ErrorCode.TRACKING_NUMBER_ALREADY_EXISTS);

        if (cancelRequestRepository.existsByOrderIdAndStatusIsNull(orderId))
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);

        deliveryRepository.save(Delivery.builder()
                .orderId(orderId)
                .courierCode(request.getCourierCode())
                .trackingNumber(request.getTrackingNumber())
                .build());

        order.ship();
    }

    // ── 5. 배송 추적 (IA nld-604) ────────────────────────────────────────────────
    @Transactional
    public TrackingResponse getTracking(Long orderId, Long memberId) {
        findOrderAsParty(orderId, memberId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
        return deliveryTracker.track(delivery);
    }

    // ── 6. 구매 확정 (IA nld-605) ────────────────────────────────────────────────
    @Transactional
    public void confirmPurchase(Long orderId, Long buyerId) {

        Order order = findOrder(orderId);

        if (!order.isBuyer(buyerId))
            throw new CustomException(ErrorCode.ORDER_UNAUTHORIZED);

        if (order.getStatus() != OrderStatus.DELIVERED)
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);

        order.complete();

        try {
            settlementService.createSettlement(
                    orderId,
                    order.getProductId(),
                    order.getSellerId(),
                    order.getTotalPrice()
            );
        } catch (Exception e) {
            log.warn("정산 생성 실패 — 거래 완료 상태는 유지됩니다. orderId={}, error={}",
                    orderId, e.getMessage());
        }
    }

    // ── 7. 취소 요청 (IA nld-606) ────────────────────────────────────────────────
    @Transactional
    public void requestCancel(Long orderId, Long requesterId, CancelRequest request) {

        Order order = findOrder(orderId);

        if (!order.isParty(requesterId))
            throw new CustomException(ErrorCode.ORDER_UNAUTHORIZED);

        if (cancelRequestRepository.existsByOrderIdAndStatusIsNull(orderId))
            throw new CustomException(ErrorCode.CANCEL_ALREADY_REQUESTED);

        order.requestCancel(request.getReason());

        cancelRequestRepository.save(OrderCancelRequest.builder()
                .orderId(orderId)
                .requesterId(requesterId)
                .reason(request.getReason())
                .build());
    }

    // ── 8. 취소 응답 (IA nld-606) ────────────────────────────────────────────────
    @Transactional
    public void respondCancel(Long orderId, Long responderId, boolean accept) {

        Order order = findOrder(orderId);

        if (!order.isParty(responderId))
            throw new CustomException(ErrorCode.ORDER_UNAUTHORIZED);

        OrderCancelRequest cancelRequest = cancelRequestRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.CANCEL_REQUEST_NOT_FOUND));

        if (accept) {
            order.confirmCancel();
            productCommandPort.restoreToOnSale(order.getProductId());
            if (order.getPaidAt() != null) {
                paymentService.refundPayment(orderId);
            }
        } else {
            cancelRequest.reject();
            order.rejectCancel();
        }
    }

    // ── 9. 마이페이지 거래 내역 (IA nld-905) ─────────────────────────────────────
    public Page<MyOrderResponse> getMyOrders(Long memberId, String type,
                                              String statusGroup, Pageable pageable) {
        boolean isBuyer  = "BUYER".equalsIgnoreCase(type);
        List<OrderStatus> statuses = toStatuses(statusGroup);

        Page<Order> orders = fetchOrderPage(isBuyer, memberId, statuses, pageable);

        List<Long> counterpartIds = orders.getContent().stream()
                .map(o -> isBuyer ? o.getSellerId() : o.getBuyerId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> nicknameMap = Map.of();
        try {
            nicknameMap = memberQueryPort.getNicknameMap(counterpartIds);
        } catch (Exception e) {
            log.warn("상대방 닉네임 배치 조회 실패 — '알 수 없음' 으로 표시. error={}", e.getMessage());
        }

        final boolean finalIsBuyer        = isBuyer;
        final Map<Long, String> nicknames = nicknameMap;

        List<MyOrderResponse> content = orders.getContent().stream()
                .map(o -> {
                    Long cpId     = finalIsBuyer ? o.getSellerId() : o.getBuyerId();
                    String cpNick = nicknames.getOrDefault(cpId, "알 수 없음");
                    return new MyOrderResponse(o, finalIsBuyer, cpNick);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orders.getTotalElements());
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────────

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Order findOrderAsParty(Long orderId, Long memberId) {
        return orderRepository.findByIdAndParty(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_UNAUTHORIZED));
    }

    private List<OrderStatus> toStatuses(String group) {
        if (group == null || group.isBlank()) return null;
        if ("거래중".equals(group))
            return List.of(OrderStatus.REQUESTED, OrderStatus.PAID,
                           OrderStatus.SHIPPING, OrderStatus.DELIVERED);
        if ("완료".equals(group)) return List.of(OrderStatus.COMPLETED);
        if ("취소".equals(group)) return List.of(OrderStatus.CANCELLED);
        return null;
    }

    private Page<Order> fetchOrderPage(boolean isBuyer, Long memberId,
                                        List<OrderStatus> statuses, Pageable pageable) {
        if (statuses == null) {
            return isBuyer
                    ? orderRepository.findByBuyerIdOrderByCreatedAtDesc(memberId, pageable)
                    : orderRepository.findBySellerIdOrderByCreatedAtDesc(memberId, pageable);
        }
        return isBuyer
                ? orderRepository.findByBuyerIdAndStatusInOrderByCreatedAtDesc(memberId, statuses, pageable)
                : orderRepository.findBySellerIdAndStatusInOrderByCreatedAtDesc(memberId, statuses, pageable);
    }
}