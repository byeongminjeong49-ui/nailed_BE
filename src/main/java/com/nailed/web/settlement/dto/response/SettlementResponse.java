package com.nailed.web.settlement.dto.response;

import com.nailed.web.settlement.entity.Settlement;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class SettlementResponse {

    private final Long          id;
    private final Long          orderId;
    private final Long          productId;
    private final String        productName;           // 정산 시점 상품명 스냅샷 (IA nld-906 정산 카드)
    private final Integer       orderAmount;
    private final Integer       commission;
    private final Integer       settlementAmount;
    private final String        sellerAccountSnapshot;
    private final String        status;
    private final String        statusLabel;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;

    public SettlementResponse(Settlement s) {
        this.id                    = s.getId();
        this.orderId               = s.getOrderId();
        this.productId             = s.getProductId();
        this.productName           = s.getProductName();
        this.orderAmount           = s.getOrderAmount();
        this.commission            = s.getCommission();
        this.settlementAmount      = s.getSettlementAmount();
        this.sellerAccountSnapshot = s.getSellerAccountSnapshot();
        this.status                = s.getStatus();
        this.statusLabel           = toLabel(s.getStatus());
        this.completedAt           = s.getCompletedAt();
        this.createdAt             = s.getCreatedAt();
    }

    private String toLabel(String status) {
        if ("PENDING".equals(status))   return "정산 예정";
        if ("COMPLETED".equals(status)) return "정산 완료";
        if ("CANCELLED".equals(status)) return "정산 취소";
        return status;
    }
}
