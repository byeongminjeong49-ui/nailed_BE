package com.nailed.web.order.dto.response;

import com.nailed.common.enums.OrderStatus;
import com.nailed.web.order.entity.Order;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class MyOrderResponse {

    private final Long          id;
    private final Long          productId;
    private final Long          counterpartId;       // 구매면 판매자 ID, 판매면 구매자 ID
    private final String        counterpartNickname; // 상대방 닉네임 (IA nld-905 거래카드)
    private final OrderStatus   status;
    private final String        statusGroup;         // 거래중 / 완료 / 취소
    private final Integer       totalPrice;
    private final LocalDateTime createdAt;

    public MyOrderResponse(Order order, boolean isBuyer, String counterpartNickname) {
        this.id                  = order.getId();
        this.productId           = order.getProductId();
        this.counterpartId       = isBuyer ? order.getSellerId() : order.getBuyerId();
        this.counterpartNickname = counterpartNickname;
        this.status              = order.getStatus();
        this.statusGroup         = toStatusGroup(order.getStatus());
        this.totalPrice          = order.getTotalPrice();
        this.createdAt           = order.getCreatedAt();
    }

    private String toStatusGroup(OrderStatus status) {
        if (status == OrderStatus.COMPLETED) return "완료";
        if (status == OrderStatus.CANCELLED) return "취소";
        return "거래중";
    }
}
