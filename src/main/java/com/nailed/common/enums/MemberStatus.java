package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {

    ACTIVE("활동중", "정상 활동 중인 회원"),
    INACTIVE("비활성", "휴면 상태인 회원"),
    WITHDRAWN("탈퇴", "탈퇴한 회원"),
    SUSPENDED("정지", "기간 정지 회원"),
    BANNED("영구정지", "영구 정지 회원");

    private final String label;
    private final String description;
}
