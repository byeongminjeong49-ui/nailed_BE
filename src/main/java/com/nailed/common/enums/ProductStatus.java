package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    ON_SALE("판매중", "상품이 판매 중인 상태"),
    SOLD_OUT("판매완료", "상품이 판매 완료된 상태"),
    DELETED("삭제됨", "상품이 삭제된 상태");

    private final String label;
    private final String description;
}
