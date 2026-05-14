package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCondition {
    NEW("New", "새제품(미사용)"),
    LIKE_NEW("Like new", "거의 새것"),
    GOOD("Good", "중고(상태 좋음)"),
    FAIR("Fair", "중고(상태 보통)"),
    WORN("Worn", "중고(사용감 많음)");

    private final String label;
    private final String description;
}
