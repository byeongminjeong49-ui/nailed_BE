package com.nailed.web.order.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.common.enums.ProductStatus;
import com.nailed.web.order.dto.OrderRequestDto;
import com.nailed.web.order.dto.OrderResponseDto;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final int DEFAULT_COMMISSION_RATE = 2;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponseDto createOrder(String buyerId, String sellerId, OrderRequestDto req) {
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("구매자와 판매자가 동일할 수 없습니다.");
        }
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다. productId=" + req.getProductId()));
        if (product.getProductStatus() != ProductStatus.ON_SALE) {
            throw new IllegalStateException("판매 중인 상품만 주문할 수 있습니다.");
        }
        // 금액 계산 순서: 수수료 → 최종 결제금액 → 판매자 정산금액
        // - 수수료(commissionAmount) = (상품가 + 배송비) × 수수료율(2%)
        // - 최종 결제금액(finalPrice) = 상품가 + 배송비 + 수수료  → 구매자가 실제로 결제하는 금액
        // - 판매자 정산금액(sellerSettlementAmount) = 최종 결제금액 - 수수료  → 배송완료 후 판매자에게 지급되는 금액
        int productPrice        = product.getPrice();
        int shippingFee         = product.getShippingFee();
        int commissionAmount    = ((productPrice + shippingFee) * DEFAULT_COMMISSION_RATE) / 100;
        int finalPrice          = productPrice + shippingFee + commissionAmount;
        int sellerSettlementAmount = finalPrice - commissionAmount;

        Order order = Order.builder()
                .orderId(generateOrderId())
                .cancelRequestStatus("NONE")
                .productId(req.getProductId())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .commission(DEFAULT_COMMISSION_RATE)       // 비율 2 저장
                .finalPrice(finalPrice)                    // 구매자 결제 금액
                .sellerSettlementAmount(sellerSettlementAmount)  // 판매자 정산 금액
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .receiverZipcode(req.getReceiverZipcode())
                .receiverAddress(req.getReceiverAddress())
                .receiverAddressDetail(req.getReceiverAddressDetail())
                .deliveryRequest(req.getDeliveryRequest())
                .build();
        order.markAsPaid();
        productRepository.updateProductStatus(req.getProductId(), ProductStatus.SOLD);
        return OrderResponseDto.from(orderRepository.save(order), shippingFee, productPrice);
    }

    public OrderResponseDto getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다. productId=" + order.getProductId()));
        return OrderResponseDto.from(order, product.getShippingFee(), product.getPrice());
    }

    public long countSellerOrdersByStatus(String sellerId, String orderStatus) {
        validateOrderStatus(orderStatus);
        return orderRepository.countBySellerIdAndOrderStatus(sellerId, orderStatus);
    }

    @Transactional
    public OrderResponseDto mockPay(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다. productId=" + order.getProductId()));
        order.markAsPaid();
        return OrderResponseDto.from(orderRepository.save(order), product.getShippingFee(), product.getPrice());
    }

    @Transactional
    public OrderResponseDto confirmOrder(String orderId, String sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
        if (!sellerId.equals(order.getSellerId())) {
            throw new IllegalStateException("판매자만 주문을 확인할 수 있습니다.");
        }
        if (!OrderStatus.PAID.name().equals(order.getOrderStatus())) {
            throw new IllegalStateException("결제완료 상태의 주문만 확인할 수 있습니다.");
        }
        order.markAsRequested();
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다."));
        return OrderResponseDto.from(orderRepository.save(order), product.getShippingFee(), product.getPrice());
    }

    @Transactional
    public OrderResponseDto cancelOrder(String orderId, String buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
        if (!buyerId.equals(order.getBuyerId())) {
            throw new IllegalStateException("구매자만 주문을 취소할 수 있습니다.");
        }
        if (!OrderStatus.PAID.name().equals(order.getOrderStatus())) {
            throw new IllegalStateException("결제완료 상태의 주문만 취소할 수 있습니다.");
        }
        orderRepository.cancelOrder(orderId);
        productRepository.updateProductStatus(order.getProductId(), ProductStatus.ON_SALE);
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다. productId=" + order.getProductId()));
        // ⚠ 재조회가 반드시 필요함 (제거하면 안 됨)
        // orderRepository.cancelOrder()는 네이티브 쿼리(@Modifying)로 DB를 직접 갱신하기 때문에
        // 영속성 컨텍스트를 거치지 않음 → 위에서 조회한 order 객체는 여전히 취소 전 상태(PAID)를 들고 있음
        // 따라서 변경된 최신 상태(orderStatus=CANCELLED, cancelledAt 등)를 응답으로 내려주려면 다시 조회해야 함
        return OrderResponseDto.from(orderRepository.findById(orderId).get(), product.getShippingFee(), product.getPrice());
    }

    // 단순 증가 방식의 주문 ID 생성 (현재 저장된 주문 수 + 1)
    private String generateOrderId() {
        long next = orderRepository.count() + 1;
        return String.format("ORDER_%03d", next);
    }

    private void validateOrderStatus(String orderStatus) {
        try {
            OrderStatus.valueOf(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 주문 상태입니다: " + orderStatus);
        }
    }
}