package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ROLE_USER("사용자", "일반 회원 역할"),
    ROLE_ADMIN("관리자", "관리자 역할");

    private final String label;
    private final String description;
}
