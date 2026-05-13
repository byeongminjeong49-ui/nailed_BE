package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    REQUESTED("Requested", "Order requested"),
    CANCEL("Cancel", "Order cancel requested"),
    PAID("Paid", "Payment completed"),
    SHIPPING("Shipping", "Delivery in progress"),
    DELIVERED("Delivered", "Delivery completed"),
    COMPLETED("Completed", "Order completed"),
    CANCELLED("Cancelled", "Order cancelled");

    private final String label;
    private final String description;
}
