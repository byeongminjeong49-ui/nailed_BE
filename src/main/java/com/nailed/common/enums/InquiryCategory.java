package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {

    MEMBER("회원", "회원 관련 문의"),
    ORDER("주문", "주문 관련 문의"),
    DELIVERY("배송", "배송 관련 문의"),
    PAYMENT("결제", "결제 관련 문의"),
    ETC("기타", "기타 문의");

    private final String label;
    private final String description;
}
