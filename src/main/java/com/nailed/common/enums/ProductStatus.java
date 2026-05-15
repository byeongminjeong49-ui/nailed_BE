package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 판매 상태
 * DB: products.product_status VARCHAR(20) DEFAULT 'ON_SALE'
 * 주석값: ON_SALE/RESERVED/SOLD/DELETED
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    ON_SALE("판매중", "상품이 판매 중인 상태"),
    RESERVED("예약중", "주문 진행 중으로 일시 잠금된 상태"),
    SOLD("판매완료", "상품이 판매 완료된 상태"),
    DELETED("삭제됨", "상품이 삭제된 상태 (soft delete)");

    private final String label;
    private final String description;
}
