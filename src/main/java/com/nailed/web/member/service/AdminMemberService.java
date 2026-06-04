package com.nailed.web.member.service;

import com.nailed.common.enums.MemberStatus;
import com.nailed.common.enums.Role;
import com.nailed.common.enums.SellerGrade;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.member.dto.AdminMemberResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public PageResponse<AdminMemberResponse.Summary> getMembers(
            String keyword,
            String role,
            String status,
            String sellerGrade,
            Pageable pageable) {
        return PageResponse.of(memberRepository.searchAdminMembers(
                blankToNull(keyword),
                parseRole(role),
                parseStatus(status),
                parseSellerGrade(sellerGrade),
                pageable
        ).map(this::toSummary));
    }

    private AdminMemberResponse.Summary toSummary(Member member) {
        return new AdminMemberResponse.Summary(
                member.getMemberId(),
                member.getUserid(),
                member.getNickname(),
                member.getRole(),
                member.getSellerGrade(),
                member.getCreatedAt(),
                member.getMemberStatus()
        );
    }

    private String parseRole(String role) {
        String value = blankToNull(role);
        if (value == null) {
            return null;
        }
        try {
            return Role.valueOf(value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String parseStatus(String status) {
        String value = blankToNull(status);
        if (value == null) {
            return null;
        }
        try {
            return MemberStatus.valueOf(value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String parseSellerGrade(String sellerGrade) {
        String value = blankToNull(sellerGrade);
        if (value == null) {
            return null;
        }
        try {
            return SellerGrade.valueOf(value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
