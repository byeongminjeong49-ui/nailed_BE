package com.nailed.domain.order.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import com.nailed.domain.order.dto.OrderRequest;
import com.nailed.domain.order.dto.OrderResponse;
import com.nailed.domain.order.entity.Order;
import com.nailed.domain.order.repository.OrderRepository;
import com.nailed.domain.product.entity.Product;
import com.nailed.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final AtomicLong sequence = new AtomicLong(1);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String placeOrder(String buyerId, OrderRequest.Place request) {
        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!"ON_SALE".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        String orderId = "ORDER_" + String.format("%03d", sequence.getAndIncrement());

        Order order = Order.builder()
                .orderId(orderId)
                .product(product)
                .buyer(buyer)
                .seller(product.getSeller())
                .productAmount(product.getPrice())
                .shippingFee(0)
                .finalPrice(product.getPrice())
                .receiverName(request.receiverName())
                .receiverPhone(request.receiverPhone())
                .receiverZipcode(request.receiverZipcode())
                .receiverAddress(request.receiverAddress())
                .receiverAddressDetail(request.receiverAddressDetail())
                .build();

        product.reserve();
        return orderRepository.save(order).getOrderId();
    }

    public OrderResponse.Detail getDetail(String orderId) {
        return OrderResponse.Detail.from(findById(orderId));
    }

    public Page<OrderResponse.Detail> getBuyerOrders(String buyerId, Pageable pageable) {
        return orderRepository.findByBuyerMemberId(buyerId, pageable)
                .map(OrderResponse.Detail::from);
    }

    public Page<OrderResponse.Detail> getSellerOrders(String sellerId, Pageable pageable) {
        return orderRepository.findBySellerMemberId(sellerId, pageable)
                .map(OrderResponse.Detail::from);
    }

    @Transactional
    public void ship(String orderId, OrderRequest.Ship request) {
        Order order = findById(orderId);
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.ship(request.carrierCode(), request.trackingNumber());
    }

    @Transactional
    public void complete(String orderId) {
        Order order = findById(orderId);
        if (!"DELIVERED".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.complete();
    }

    @Transactional
    public void requestCancel(String orderId, OrderRequest.CancelRequest request) {
        Order order = findById(orderId);
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.requestCancel(request.reason());
    }

    @Transactional
    public void respondCancel(String orderId, OrderRequest.CancelResponse request) {
        Order order = findById(orderId);
        if (!"CANCEL_REQUESTED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (request.accept()) {
            order.acceptCancel();
            order.getProduct().completeSale();
        } else {
            order.rejectCancel();
        }
    }

    private Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}
