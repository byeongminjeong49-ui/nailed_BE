package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCondition {

    NEW("새제품", "새제품(미사용)"),
    LIKE_NEW("거의 새것", "거의 새것"),
    GOOD("상태 좋음", "중고(상태 좋음)"),
    FAIR("상태 보통", "중고(상태 보통)"),
    WORN("사용감 많음", "중고(사용감 많음)");

    private final String label;
    private final String description;
}
