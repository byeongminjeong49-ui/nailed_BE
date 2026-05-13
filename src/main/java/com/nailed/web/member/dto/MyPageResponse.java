package com.nailed.web.member.dto;

import com.nailed.common.enums.MemberStatus;
import com.nailed.common.enums.Role;
import com.nailed.web.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPageResponse {

    private Long memberId;
    private String email;
    private String nickname;
    private String phoneNumber;
    private Role role;
    private MemberStatus memberStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MyPageResponse from(Member member) {
        return MyPageResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole())
                .memberStatus(member.getMemberStatus())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
