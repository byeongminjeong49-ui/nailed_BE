package com.nailed.web.order.dto.response;

import com.nailed.common.enums.OrderStatus;
import com.nailed.web.order.entity.Order;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class OrderListResponse {

    private final Long          id;
    private final Long          productId;
    private final OrderStatus   status;
    private final Integer       totalPrice;
    private final LocalDateTime createdAt;

    public OrderListResponse(Order order) {
        this.id         = order.getId();
        this.productId  = order.getProductId();
        this.status     = order.getStatus();
        this.totalPrice = order.getTotalPrice();
        this.createdAt  = order.getCreatedAt();
    }

} //class
