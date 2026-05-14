package com.nailed.web.member.service;

import com.nailed.web.settlement.service.MemberQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Settlement 도메인의 MemberQueryPort 구현체.
 * member 도메인 패키지에 위치 (Anti-Corruption Layer)
 *
 * TODO: MemberRepository 주입받아 실제 회원 정보 조회 로직 구현
 */
@Component
@RequiredArgsConstructor
public class SettlementMemberQueryPortImpl implements MemberQueryPort {

    // TODO: 실제 구현 시 MemberRepository 주입
    // private final MemberRepository memberRepository;

    /**
     * 판매자 계좌 정보 조회 (정산 생성 시 스냅샷 복사용)
     */
    @Override
    public AccountInfo getSellerAccount(Long sellerId) {
        // TODO: memberRepository.findById(sellerId)로 실제 계좌 조회
        return new AccountInfo("국민은행", "123-456-789012");
    }

    /**
     * 판매자 닉네임 배치 조회 (N+1 방지)
     */
    @Override
    public Map<Long, String> getNicknameMap(List<Long> sellerIds) {
        // TODO: IN 쿼리로 닉네임 일괄 조회
        // SELECT member_id, nickname FROM members WHERE member_id IN (:sellerIds)
        return sellerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> "판매자" + id  // 더미 닉네임
                ));
    }
}