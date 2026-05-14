package com.nailed.web.member.service;

import com.nailed.common.enums.MemberStatus;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.settlement.service.MemberQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementMemberQueryPortImpl implements MemberQueryPort {

    private final MemberRepository memberRepository;

    @Override
    public AccountInfo getSellerAccount(Long sellerId) {
        return null;
    }

    @Override
    public Map<Long, String> getNicknameMap(List<Long> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return Map.of();
        }

        return StreamSupport.stream(memberRepository.findAllById(sellerIds).spliterator(), false)
                .filter(member -> !member.isDeleted())
                .filter(member -> member.getMemberStatus() == MemberStatus.ACTIVE)
                .collect(Collectors.toMap(Member::getId, Member::getNickname, (left, right) -> left));
    }
}
