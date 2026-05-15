package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("대기중", "결제 대기 중"),
    FAILED("실패", "결제 실패"),
    COMPLETED("완료", "결제가 완료된 상태"),
    REFUNDED("환불", "결제가 환불된 상태");

    private final String label;
    private final String description;
}
