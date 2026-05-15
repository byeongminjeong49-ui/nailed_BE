package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {

    READY("배송 준비중", "운송장 등록 전 배송 대기 상태"),
    SHIPPING("배송중", "상품이 배송 중인 상태"),
    DELIVERED("배송 완료", "상품이 수령지에 도착한 상태");

    private final String label;
    private final String description;
}
