package com.nailed.web.product.service;

import com.nailed.web.order.service.ProductCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Order 도메인의 ProductCommandPort 구현체.
 * product 도메인 패키지에 위치 (Anti-Corruption Layer)
 *
 * TODO: ProductRepository 주입받아 실제 비관적 락 로직 구현
 *       @Lock(LockModeType.PESSIMISTIC_WRITE) 필수!
 */
@Component
@RequiredArgsConstructor
public class ProductCommandPortImpl implements ProductCommandPort {

    // TODO: 실제 구현 시 ProductRepository 주입
    // private final ProductRepository productRepository;

    /**
     * 상품 정보 조회 (일반 조회, 락 불필요)
     */
    @Override
    public ProductInfo getProductInfo(Long productId) {
        // TODO: productRepository.findById(productId)
        return new ProductInfo(productId, 1L, 10000);  // 더미 데이터
    }

    /**
     * 비관적 쓰기 락으로 상품 정보 조회 (동시 구매 방지)
     */
    @Override
    public ProductInfo lockAndGetProductInfo(Long productId) {
        // TODO: @Lock(LockModeType.PESSIMISTIC_WRITE) 적용된 메서드 호출 필요
        return new ProductInfo(productId, 1L, 10000);  // 더미 데이터
    }

    /**
     * 판매 완료 상태로 변경
     */
    @Override
    public void markAsSoldOut(Long productId) {
        // TODO: product.changeStatus(SOLD_OUT)
        System.out.println("[DUMMY] markAsSoldOut: " + productId);
    }

    /**
     * 다시 판매중 상태로 복원
     */
    @Override
    public void restoreToOnSale(Long productId) {
        // TODO: product.changeStatus(ON_SALE)
        System.out.println("[DUMMY] restoreToOnSale: " + productId);
    }
}