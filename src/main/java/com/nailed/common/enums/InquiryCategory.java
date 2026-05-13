package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {
    MEMBER("Member", "Member inquiry"),
    ORDER("Order", "Order inquiry"),
    DELIVERY("Delivery", "Delivery inquiry"),
    PAYMENT("Payment", "Payment inquiry"),
    ETC("Etc", "Etc inquiry");

    private final String label;
    private final String description;
}
