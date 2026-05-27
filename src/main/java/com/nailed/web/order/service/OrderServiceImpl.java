package com.nailed.web.order.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.web.order.dto.OrderRequestDto;
import com.nailed.web.order.dto.OrderResponseDto;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_ID_PREFIX = "ORDER_";
    private static final int DEFAULT_COMMISSION_RATE = 2;

    private final OrderRepository orderRepository;
    
    @Override
    @Transactional
    public OrderResponseDto createOrder(String buyerId, String sellerId, OrderRequestDto req) {
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("구매자와 판매자가 동일할 수 없습니다.");
        }

        Order order = Order.builder()
                .orderId(generateOrderId())
                .productId(req.getProductId())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .commission(DEFAULT_COMMISSION_RATE)
                .productAmount(req.getProductAmount())
                .shippingFee(req.getShippingFee())
                .finalPrice(req.calcFinalPrice())
                .sellerSettlementAmount(req.calcSettlementAmount(DEFAULT_COMMISSION_RATE))
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .receiverZipcode(req.getReceiverZipcode())
                .receiverAddress(req.getReceiverAddress())
                .receiverAddressDetail(req.getReceiverAddressDetail())
                .deliveryRequest(req.getDeliveryRequest())
                .build();

        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Override
    public OrderResponseDto getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
        return OrderResponseDto.from(order);
    }

    @Override
    public long countSellerOrdersByStatus(String sellerId, String orderStatus) {
        validateOrderStatus(orderStatus);
        return orderRepository.countBySellerIdAndOrderStatus(sellerId, orderStatus);
    }
    
    @Override
    @Transactional
    public OrderResponseDto mockPay(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
        order.markAsPaid(); 
        
        return OrderResponseDto.from(orderRepository.save(order));
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
