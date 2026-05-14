package com.nailed.web.settlement.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.settlement.dto.response.AdminSettlementResponse;
import com.nailed.web.settlement.dto.response.SettlementResponse;
import com.nailed.web.settlement.entity.Settlement;
import com.nailed.web.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final MemberQueryPort      memberQueryPort;
    private final ProductQueryPort     productQueryPort;   // 상품명 스냅샷 조회용

    /**
     * 구매 확정 시 정산 레코드 자동 생성 (IA nld-605)
     *
     * REQUIRES_NEW: OrderService 트랜잭션과 독립 실행.
     * 정산 저장 실패 시에도 거래 완료(order.complete())는 롤백되지 않음.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSettlement(Long orderId, Long productId, Long sellerId, Integer orderAmount) {

        if (settlementRepository.existsByOrderId(orderId)) {
            log.warn("정산 레코드가 이미 존재합니다. orderId={}", orderId);
            return;
        }

        // 상품명 스냅샷 (IA nld-906: 정산 카드 "상품명") — 계좌 스냅샷과 동일한 패턴
        String productName = null;
        try {
            productName = productQueryPort.getProductName(productId);
        } catch (Exception e) {
            log.warn("상품명 조회 실패 — null 로 저장. productId={}, error={}", productId, e.getMessage());
        }

        // 판매자 계좌 스냅샷 (IA nld-605 / nld-906)
        String accountSnapshot = null;
        try {
            MemberQueryPort.AccountInfo account = memberQueryPort.getSellerAccount(sellerId);
            if (account != null) accountSnapshot = account.toSnapshot();
        } catch (Exception e) {
            log.warn("판매자 계좌 조회 실패 — null 로 저장. sellerId={}, error={}", sellerId, e.getMessage());
        }

        settlementRepository.save(Settlement.builder()
                .orderId(orderId)
                .productId(productId)
                .sellerId(sellerId)
                .orderAmount(orderAmount)
                .productName(productName)
                .sellerAccountSnapshot(accountSnapshot)
                .build());
    }

    /**
     * 정산 취소 — 거래 취소(nld-606) 연동: PENDING → CANCELLED
     */
    @Transactional
    public void cancelSettlement(Long orderId) {
        settlementRepository.findByOrderId(orderId).ifPresent(s -> {
            if ("PENDING".equals(s.getStatus())) {
                s.cancel();
            } else {
                log.warn("PENDING 이 아닌 정산 취소 시도. orderId={}, status={}", orderId, s.getStatus());
            }
        });
    }

    // ── 마이페이지: 판매자 정산 내역 (IA nld-906) ────────────────────────────────
    public Page<SettlementResponse> getMySettlements(Long sellerId, String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return settlementRepository
                    .findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status.toUpperCase(), pageable)
                    .map(SettlementResponse::new);
        }
        return settlementRepository
                .findBySellerIdOrderByCreatedAtDesc(sellerId, pageable)
                .map(SettlementResponse::new);
    }

    // ── 관리자: 정산 목록 다중 조건 조회 (IA nld-1110) ──────────────────────────
    public Page<AdminSettlementResponse> getSettlementsForAdmin(
            String status, Long sellerId, LocalDateTime from, LocalDateTime to, Pageable pageable) {

        Page<Settlement> page = settlementRepository.findByAdminFilter(status, sellerId, from, to, pageable);

        List<Long> sellerIds = page.getContent().stream()
                .map(Settlement::getSellerId).distinct().collect(Collectors.toList());

        Map<Long, String> nicknameMap = Map.of();
        try {
            nicknameMap = memberQueryPort.getNicknameMap(sellerIds);
        } catch (Exception e) {
            log.warn("판매자 닉네임 배치 조회 실패. error={}", e.getMessage());
        }

        final Map<Long, String> nicknames = nicknameMap;
        List<AdminSettlementResponse> content = page.getContent().stream()
                .map(s -> new AdminSettlementResponse(
                        s, nicknames.getOrDefault(s.getSellerId(), "알 수 없음")))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    // ── 관리자: 단건 정산 완료 처리 (IA nld-1110) ───────────────────────────────
    @Transactional
    public void completeSettlement(Long settlementId) {
        Settlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!"PENDING".equals(s.getStatus()))
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        s.complete();
    }

    // ── 관리자: 다건 일괄 정산 완료 처리 (IA nld-1110) ──────────────────────────
    @Transactional
    public void completeSettlements(List<Long> settlementIds) {
        settlementIds.forEach(this::completeSettlement);
    }
}
