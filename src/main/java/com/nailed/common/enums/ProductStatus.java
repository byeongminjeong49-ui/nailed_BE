package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ON_SALE("On sale", "Product is on sale"),
    SOLD_OUT("Sold out", "Product is sold out"),
    DELETED("Deleted", "Product is deleted");

    private final String label;
    private final String description;
}
