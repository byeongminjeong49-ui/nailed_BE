package com.nailed.web.order.service;

import java.util.List;
import java.util.Map;

/**
 * Member 도메인과의 Anti-Corruption Layer (order 도메인 전용)
 *
 * 구현체(MemberQueryPortImpl)는 member 도메인 패키지에 위치.
 * settlement 도메인의 MemberQueryPort 와 별도 인터페이스로 분리
 * (도메인 간 직접 의존 방지).
 */
public interface MemberQueryPort {

    /**
     * 닉네임 배치 조회 (N+1 방지)
     * getMyOrders() 에서 페이지 내 counterpartId 목록을 IN 쿼리 1회로 조회.
     *
     * @param memberIds 회원 ID 목록
     * @return { memberId → nickname } 맵. 조회 실패한 ID 는 "알 수 없음" 대체.
     */
    Map<Long, String> getNicknameMap(List<Long> memberIds);
}
