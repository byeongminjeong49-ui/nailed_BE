package com.nailed.web.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberWithdrawResponse {

    private Long memberId;
    private String message;

    public static MemberWithdrawResponse of(Long memberId) {
        return MemberWithdrawResponse.builder()
                .memberId(memberId)
                .message("Member withdrawal completed.")
                .build();
    }
}
