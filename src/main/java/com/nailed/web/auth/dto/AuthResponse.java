package com.nailed.web.auth.dto;

import com.nailed.web.member.entity.Member;

public class AuthResponse {

    public record DuplicateCheck(
            boolean duplicated
    ) {}

    public record Signup(
            String memberId,
            String email,
            String nickname,
            String name
    ) {
        public static Signup from(Member member) {
            return new Signup(
                    member.getMemberId(),
                    member.getEmail(),
                    member.getNickname(),
                    member.getName()
            );
        }
    }

    public record Login(
            String memberId,
            String email,
            String nickname,
            String role,
            String accessToken,
            String refreshToken,
            String tokenType
    ) {
        public static Login of(Member member, String accessToken, String refreshToken) {
            return new Login(
                    member.getMemberId(),
                    member.getEmail(),
                    member.getNickname(),
                    member.getRole(),
                    accessToken,
                    refreshToken,
                    "Bearer"
            );
        }
    }

    public record SimpleResult(
            boolean success
    ) {}
}
