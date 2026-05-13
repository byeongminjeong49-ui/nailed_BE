package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("Pending", "Payment pending"),
    FAILED("Failed", "Payment failed"),
    COMPLETED("Completed", "Payment completed"),
    REFUNDED("Refunded", "Payment refunded");

    private final String label;
    private final String description;
}
