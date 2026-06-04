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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final String ORDER_ID_PREFIX = "ORDER_";
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
        int productPrice        = product.getPrice();
        int shippingFee         = product.getShippingFee();
        int commissionAmount    = ((productPrice + shippingFee) * DEFAULT_COMMISSION_RATE) / 100;
        int finalPrice          = productPrice + shippingFee + commissionAmount;
        int sellerSettlementAmount = (int) (Math.round((finalPrice - ((finalPrice * DEFAULT_COMMISSION_RATE) / 100.0)) / 10.0) * 10);

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
        if (!"PAID".equals(order.getOrderStatus())) {
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
        return OrderResponseDto.from(orderRepository.findById(orderId).get(), product.getShippingFee(), product.getPrice());
    }

    private String generateOrderId() {
        long num = orderRepository.count() + 1;
        String candidateId;
        do {
            candidateId = ORDER_ID_PREFIX + String.format("%03d", num++);
        } while (orderRepository.existsById(candidateId));
        return candidateId;
    }

    private void validateOrderStatus(String orderStatus) {
        try {
            OrderStatus.valueOf(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 주문 상태입니다: " + orderStatus);
        }
    }
}