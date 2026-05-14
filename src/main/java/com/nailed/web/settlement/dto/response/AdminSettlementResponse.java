package com.nailed.web.settlement.dto.response;

import com.nailed.web.settlement.entity.Settlement;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AdminSettlementResponse {

    private final Long          id;
    private final Long          orderId;
    private final Long          productId;
    private final Long          sellerId;
    private final String        sellerNickname;        // 판매자 닉네임 (IA nld-1110 정산 카드)
    private final Integer       orderAmount;
    private final Integer       commission;
    private final Integer       settlementAmount;
    private final String        sellerAccountSnapshot;
    private final String        status;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;

    public AdminSettlementResponse(Settlement s, String sellerNickname) {
        this.id                    = s.getId();
        this.orderId               = s.getOrderId();
        this.productId             = s.getProductId();
        this.sellerId              = s.getSellerId();
        this.sellerNickname        = sellerNickname;
        this.orderAmount           = s.getOrderAmount();
        this.commission            = s.getCommission();
        this.settlementAmount      = s.getSettlementAmount();
        this.sellerAccountSnapshot = s.getSellerAccountSnapshot();
        this.status                = s.getStatus();
        this.completedAt           = s.getCompletedAt();
        this.createdAt             = s.getCreatedAt();
    }
}
