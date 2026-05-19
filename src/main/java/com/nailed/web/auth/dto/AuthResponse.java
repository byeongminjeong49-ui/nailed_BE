package com.nailed.web.auth.dto;

import com.nailed.web.member.entity.Member;

public class AuthResponse {

    public record DuplicateCheck(
            boolean duplicated
    ) {}

    public record Signup(
            String memberId,
            String userid,
            String nickname,
            String name
    ) {
        public static Signup from(Member member) {
            return new Signup(
                    member.getMemberId(),
                    member.getUserid(),
                    member.getNickname(),
                    member.getName()
            );
        }
    }

    public record Login(
            String memberId,
            String userid,
            String nickname,
            String role,
            String accessToken
    ) {
        public static Login from(Member member, String accessToken) {
            return new Login(
                    member.getMemberId(),
                    member.getUserid(),
                    member.getNickname(),
                    member.getRole(),
                    accessToken
            );
        }
    }

    public record PasswordReset(
            String temporaryPassword
    ) {}

    public record SimpleResult(
            boolean success
    ) {}
}
