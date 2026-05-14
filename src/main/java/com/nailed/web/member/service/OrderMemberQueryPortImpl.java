package com.nailed.web.member.service;

<<<<<<< HEAD
import com.nailed.common.enums.MemberStatus;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.order.service.MemberQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
=======
import com.nailed.web.order.service.MemberQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
>>>>>>> main

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
<<<<<<< HEAD
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderMemberQueryPortImpl implements MemberQueryPort {

    private final MemberRepository memberRepository;

    @Override
    public Map<Long, String> getNicknameMap(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        return StreamSupport.stream(memberRepository.findAllById(memberIds).spliterator(), false)
                .filter(member -> !member.isDeleted())
                .filter(member -> member.getMemberStatus() == MemberStatus.ACTIVE)
                .collect(Collectors.toMap(Member::getId, Member::getNickname, (left, right) -> left));
    }
}
=======

/**
 * Order 도메인의 MemberQueryPort 구현체.
 * member 도메인 패키지에 위치 (Anti-Corruption Layer)
 *
 * TODO: MemberRepository 주입받아 실제 회원 정보 조회 로직 구현
 */
@Component
@RequiredArgsConstructor
public class OrderMemberQueryPortImpl implements MemberQueryPort {

    // TODO: 실제 구현 시 MemberRepository 주입
    // private final MemberRepository memberRepository;

    /**
     * 회원 닉네임 배치 조회 (N+1 방지)
     */
    @Override
    public Map<Long, String> getNicknameMap(List<Long> memberIds) {
        // TODO: IN 쿼리로 닉네임 일괄 조회
        // SELECT member_id, nickname FROM members WHERE member_id IN (:memberIds)
        return memberIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> "회원" + id  // 더미 닉네임
                ));
    }
}
>>>>>>> main
