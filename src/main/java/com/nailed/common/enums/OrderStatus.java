package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    REQUESTED("주문접수", "주문이 접수된 상태"),
    CANCEL("취소요청", "주문 취소를 요청한 상태"),
    PAID("결제완료", "결제가 완료된 상태"),
    SHIPPING("배송중", "상품이 배송 중인 상태"),
    DELIVERED("배송완료", "상품이 배송 완료된 상태"),
    COMPLETED("구매확정", "구매자가 상품을 확인하고 거래를 완료한 상태"),
    CANCELLED("취소됨", "주문이 취소된 상태");

    private final String label;
    private final String description;
}
