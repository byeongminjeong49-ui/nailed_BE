package com.nailed.web.settlement.service;

/**
 * Product 도메인과의 Anti-Corruption Layer (읽기 전용)
 *
 * 구현체(ProductQueryPortImpl)는 product 도메인 패키지에 위치하며
 * ProductRepository 를 통해 상품명을 조회한다.
 *
 * 정산 생성 시 productName 을 스냅샷으로 저장.
 * 이후 상품이 삭제·수정되어도 정산 내역의 상품명은 유지된다.
 * (판매자 계좌 스냅샷과 동일한 패턴)
 */
public interface ProductQueryPort {

    /**
     * 상품명 조회
     *
     * @param productId 상품 ID
     * @return 상품명. 조회 실패 시 null 반환 (정산 생성 자체는 중단하지 않음).
     */
    String getProductName(Long productId);
}
