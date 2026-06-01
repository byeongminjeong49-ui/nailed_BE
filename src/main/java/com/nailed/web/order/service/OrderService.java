package com.nailed.web.order.service;

import com.nailed.web.order.dto.OrderRequestDto;
import com.nailed.web.order.dto.OrderResponseDto;

public interface OrderService {

    OrderResponseDto createOrder(String buyerId, String sellerId, OrderRequestDto requestDto);

    OrderResponseDto getOrder(String orderId);

    long countSellerOrdersByStatus(String sellerId, String orderStatus);
    
    OrderResponseDto mockPay(String orderId);
    
    OrderResponseDto confirmOrder(String orderId, String sellerId);
    
    OrderResponseDto cancelOrder(String orderId, String buyerId);
    
}
