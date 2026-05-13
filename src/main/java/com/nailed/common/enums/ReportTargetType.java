package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    MEMBER("Member", "Member report"),
    PRODUCT("Product", "Product report");

    private final String label;
    private final String description;
}
