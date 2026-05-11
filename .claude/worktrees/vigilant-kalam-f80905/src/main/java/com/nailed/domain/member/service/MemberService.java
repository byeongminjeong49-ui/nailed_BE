package com.nailed.domain.member.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.dto.MemberRequest;
import com.nailed.domain.member.dto.MemberResponse;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse.Detail getMyProfile(String memberId) {
        Member member = findById(memberId);
        return MemberResponse.Detail.from(member);
    }

    @Transactional
    public void updateProfile(String memberId, MemberRequest.UpdateProfile request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            Member existing = memberRepository.findByNickname(request.nickname())
                    .orElseThrow();
            if (!existing.getMemberId().equals(memberId)) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }
        Member member = findById(memberId);
        member.updateProfile(request.nickname(), request.phone(), request.shopInfo(), request.marketingAgreed());
    }

    @Transactional
    public void updateBankInfo(String memberId, MemberRequest.UpdateBankInfo request) {
        Member member = findById(memberId);
        member.updateBankInfo(request.bankCode(), request.accountNumber(), request.depositorName());
    }

    @Transactional
    public void withdraw(String memberId) {
        Member member = findById(memberId);
        member.withdraw();
    }

    private Member findById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
