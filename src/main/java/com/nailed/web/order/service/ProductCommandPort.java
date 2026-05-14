package com.nailed.web.order.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Product 도메인과의 Anti-Corruption Layer
 *
 * 구현체(ProductCommandPortImpl)는 product 도메인 패키지에 위치하며
 * JPA @Lock(PESSIMISTIC_WRITE) 로 비관적 락을 적용한다.
 */
public interface ProductCommandPort {

    /**
     * 상품 정보 조회 (일반 조회, 배송 추적 등 락 불필요한 경우)
     */
    ProductInfo getProductInfo(Long productId);

    /**
     * 비관적 쓰기 락(SELECT … FOR UPDATE)을 걸고 상품 정보 조회 (IA nld-403 동시 구매 방지)
     *
     * 구현체에서 반드시 @Lock(LockModeType.PESSIMISTIC_WRITE) 를 적용해야 한다.
     * 락 획득 실패(타임아웃) 시 PessimisticLockingFailureException → 409 응답으로 매핑.
     */
    ProductInfo lockAndGetProductInfo(Long productId);

    void markAsSoldOut(Long productId);
    void restoreToOnSale(Long productId);

    @Getter
    @AllArgsConstructor
    class ProductInfo {
        private Long    productId;
        private Long    sellerId;
        private Integer price;
    }
}
