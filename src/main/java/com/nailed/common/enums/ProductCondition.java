package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCondition {
    NEW("New", "New product"),
    LIKE_NEW("Like new", "Like new product"),
    GOOD("Good", "Good used product"),
    FAIR("Fair", "Fair used product");

    private final String label;
    private final String description;
}
