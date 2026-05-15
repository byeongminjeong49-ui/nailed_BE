package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsType {

    SERVICE("서비스", "서비스 약관"),
    PRIVACY("개인정보", "개인정보 처리방침"),
    MARKETING("마케팅", "마케팅 정보 수신 동의");

    private final String label;
    private final String description;
}
