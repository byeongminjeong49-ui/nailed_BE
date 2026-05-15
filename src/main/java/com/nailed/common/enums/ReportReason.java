package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {

    SCAM("사기", "사기 의심 상품"),
    FAKE("가품", "가품 의심 상품"),
    INAPPROPRIATE("부적절함", "부적절한 콘텐츠"),
    ILLEGAL("불법", "불법 콘텐츠"),
    ETC("기타", "기타 신고");

    private final String label;
    private final String description;
}
