package com.nailed.web.order.service;
import com.nailed.common.enums.ProductStatus;
import com.nailed.web.order.dto.OrderResponseDto;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.member.service.SellerGradeService;
import com.nailed.web.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MockShippingServiceImpl implements ShippingService {
    private final OrderRepository orderRepository;
    private final SellerGradeService sellerGradeService;
    private final ProductRepository productRepository; // ← 추가

    // 운송장 등록 — PAID 상태인 주문만 가능
    @Override
    public OrderResponseDto registerTracking(String orderId, String carrierCode, String trackingNumber) {
        Order order = findOrder(orderId);
        if (!"PAID".equals(order.getOrderStatus())) {
            throw new IllegalStateException("결제 완료 상태의 주문만 운송장을 등록할 수 있습니다.");
        }
        order.startShipping(carrierCode, trackingNumber);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 배송 완료 처리 — mock이므로 즉시 DELIVERED 로 변경
    @Override
    public OrderResponseDto confirmDelivery(String orderId) {
        Order order = findOrder(orderId);
        if (!"SHIPPING".equals(order.getOrderStatus())) {
            throw new IllegalStateException("배송 중 상태의 주문만 배송 완료 처리할 수 있습니다.");
        }
        order.markAsDelivered(); // DELIVERED + deliveredAt 기록
        // seller_settlement_amount는 이미 주문 생성 시 계산됨
        // DELIVERED = 정산 확정 (안전결제 에스크로 해제 시점)
        Order savedOrder = orderRepository.save(order);
        productRepository.updateProductStatus(savedOrder.getProductId(), ProductStatus.SOLD); // ← 추가
        sellerGradeService.refreshSellerGrade(savedOrder.getSellerId());
        return OrderResponseDto.from(savedOrder);
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
    }
}