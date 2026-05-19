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
            String role
    ) {
        public static Login from(Member member) {
            return new Login(
                    member.getMemberId(),
                    member.getEmail(),
                    member.getNickname(),
                    member.getRole()
            );
        }
    }

    public record SimpleResult(
            boolean success
    ) {}
}