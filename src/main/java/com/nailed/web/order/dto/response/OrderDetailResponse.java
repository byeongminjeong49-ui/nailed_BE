package com.nailed.web.order.dto.response;

import com.nailed.common.enums.OrderStatus;
import com.nailed.web.order.entity.Order;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class OrderDetailResponse {

    private final Long          id;
    private final Long          buyerId;
    private final Long          sellerId;
    private final Long          productId;
    private final OrderStatus   status;
    private final Integer       totalPrice;
    private final String        cancelReason;
    private final LocalDateTime paidAt;
    private final LocalDateTime shippedAt;
    private final LocalDateTime deliveredAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;

    public OrderDetailResponse(Order order) {
        this.id           = order.getId();
        this.buyerId      = order.getBuyerId();
        this.sellerId     = order.getSellerId();
        this.productId    = order.getProductId();
        this.status       = order.getStatus();
        this.totalPrice   = order.getTotalPrice();
        this.cancelReason = order.getCancelReason();
        this.paidAt       = order.getPaidAt();
        this.shippedAt    = order.getShippedAt();
        this.deliveredAt  = order.getDeliveredAt();
        this.completedAt  = order.getCompletedAt();
        this.cancelledAt  = order.getCancelledAt();
        this.createdAt    = order.getCreatedAt();
    }

} //class
