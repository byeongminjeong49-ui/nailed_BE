package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {
    READY("Ready", "Delivery is ready"),
    IN_TRANSIT("In transit", "Delivery is in transit"),
    DELIVERED("Delivered", "Delivery is completed");

    private final String label;
    private final String description;
}
