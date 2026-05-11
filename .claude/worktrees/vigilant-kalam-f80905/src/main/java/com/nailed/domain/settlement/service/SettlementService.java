package com.nailed.domain.settlement.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.payment.entity.Payment;
import com.nailed.domain.payment.repository.PaymentRepository;
import com.nailed.domain.settlement.dto.SettlementResponse;
import com.nailed.domain.settlement.entity.Settlement;
import com.nailed.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private static final AtomicLong sequence = new AtomicLong(1);
    private static final double COMMISSION_RATE = 0.02;

    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public String createSettlement(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        int commission = (int) (payment.getAmount() * COMMISSION_RATE);
        int amount = payment.getAmount() - commission;

        String settlementId = "SETL_" + String.format("%03d", sequence.getAndIncrement());

        Settlement settlement = Settlement.builder()
                .settlementId(settlementId)
                .payment(payment)
                .seller(payment.getOrder().getSeller())
                .amount(amount)
                .commission(commission)
                .bankCode(payment.getOrder().getSeller().getBankCode())
                .accountNumber(payment.getOrder().getSeller().getAccountNumber())
                .depositorName(payment.getOrder().getSeller().getDepositorName())
                .build();

        return settlementRepository.save(settlement).getSettlementId();
    }

    public Page<SettlementResponse.Detail> getSellerSettlements(String sellerId, Pageable pageable) {
        return settlementRepository.findBySellerMemberId(sellerId, pageable)
                .map(SettlementResponse.Detail::from);
    }

    @Transactional
    public void complete(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));
        settlement.complete();
    }
}
