package com.nailed.web.settlement.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Member 도메인과의 Anti-Corruption Layer (읽기 전용)
 * 구현체(MemberQueryPortImpl)는 member 도메인 패키지에 위치.
 */
public interface MemberQueryPort {

    /**
     * 판매자 계좌 정보 조회 (정산 생성 시 스냅샷 복사용)
     */
    AccountInfo getSellerAccount(Long sellerId);

    /**
     * 판매자 닉네임 배치 조회 (N+1 방지)
     * 관리자 정산 목록에서 20건을 한 번의 IN 쿼리로 조회.
     *
     * @param sellerIds 판매자 ID 목록
     * @return { sellerId → nickname } 맵. 조회 실패한 ID는 "알 수 없음"으로 대체.
     */
    Map<Long, String> getNicknameMap(List<Long> sellerIds);

    @Getter
    @AllArgsConstructor
    class AccountInfo {
        private String bankName;
        private String accountNumber;

        /** "국민은행 123-456-789012" 형태 스냅샷 문자열 반환 */
        public String toSnapshot() {
            return bankName + " " + accountNumber;
        }
    }
}
