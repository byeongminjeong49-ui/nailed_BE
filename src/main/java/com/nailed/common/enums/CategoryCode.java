package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryCode {
    CLOTHES_TOP("Top", "Clothes > Top"),
    CLOTHES_BOTTOM("Bottom", "Clothes > Bottom"),
    CLOTHES_OUTER("Outer", "Clothes > Outer"),
    CLOTHES_SHOES("Shoes", "Clothes > Shoes"),
    CLOTHES_ACC("Accessories", "Clothes > Accessories"),
    IT_LAPTOP("Laptop", "IT > Laptop"),
    IT_PHONE("Phone", "IT > Phone"),
    IT_TABLET("Tablet", "IT > Tablet"),
    IT_PERIPHERAL("Peripheral", "IT > Peripheral");

    private final String label;
    private final String description;
}
