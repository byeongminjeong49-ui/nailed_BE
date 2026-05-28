package com.nailed.web.member.dto;

import java.time.LocalDateTime;

public class AdminMemberResponse {

    public record Summary(
            String memberId,
            String userid,
            String nickname,
            String role,
            String sellerGrade,
            LocalDateTime createdAt,
            String status
    ) {}
}
