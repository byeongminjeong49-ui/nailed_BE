package com.nailed.domain.member.dto;

import com.nailed.domain.member.entity.Member;

import java.time.LocalDateTime;

public class MemberResponse {

    public record Detail(
            String memberId,
            String email,
            String nickname,
            String name,
            String phone,
            String shopInfo,
            String status,
            String sellerGrade,
            String role,
            boolean marketingAgreed,
            LocalDateTime lastLoginAt,
            int loginCount,
            LocalDateTime createdAt
    ) {
        public static Detail from(Member member) {
            return new Detail(
                    member.getMemberId(),
                    member.getEmail(),
                    member.getNickname(),
                    member.getName(),
                    member.getPhone(),
                    member.getShopInfo(),
                    member.getStatus(),
                    member.getSellerGrade(),
                    member.getRole(),
                    member.isMarketingAgreed(),
                    member.getLastLoginAt(),
                    member.getLoginCount(),
                    member.getCreatedAt()
            );
        }
    }

    public record Summary(
            String memberId,
            String nickname,
            String sellerGrade
    ) {
        public static Summary from(Member member) {
            return new Summary(
                    member.getMemberId(),
                    member.getNickname(),
                    member.getSellerGrade()
            );
        }
    }
}
