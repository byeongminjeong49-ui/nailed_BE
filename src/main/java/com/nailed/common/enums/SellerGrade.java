package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerGrade {
    BRONZE("Bronze", "Bronze seller"),
    SILVER("Silver", "Silver seller"),
    GOLD("Gold", "Gold seller"),
    DIAMOND("Diamond", "Diamond seller");

    private final String label;
    private final String description;
}
