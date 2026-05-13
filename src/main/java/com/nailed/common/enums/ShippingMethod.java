package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShippingMethod {
    COURIER("Courier", "Courier delivery");

    private final String label;
    private final String description;
}
