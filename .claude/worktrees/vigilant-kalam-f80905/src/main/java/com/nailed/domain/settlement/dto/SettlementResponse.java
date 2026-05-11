package com.nailed.domain.settlement.dto;

import com.nailed.domain.settlement.entity.Settlement;

import java.time.LocalDateTime;

public class SettlementResponse {

    public record Detail(
            String settlementId,
            String paymentId,
            String sellerId,
            int amount,
            int commission,
            String status,
            String bankCode,
            String accountNumber,
            String depositorName,
            LocalDateTime completedAt,
            LocalDateTime createdAt
    ) {
        public static Detail from(Settlement settlement) {
            return new Detail(
                    settlement.getSettlementId(),
                    settlement.getPayment().getPaymentId(),
                    settlement.getSeller().getMemberId(),
                    settlement.getAmount(),
                    settlement.getCommission(),
                    settlement.getStatus(),
                    settlement.getBankCode(),
                    settlement.getAccountNumber(),
                    settlement.getDepositorName(),
                    settlement.getCompletedAt(),
                    settlement.getCreatedAt()
            );
        }
    }
}
