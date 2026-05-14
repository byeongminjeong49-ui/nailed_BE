package com.nailed.web.product.service;

import com.nailed.web.settlement.service.ProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Settlement 도메인의 ProductQueryPort 구현체.
 * product 도메인 패키지에 위치 (Anti-Corruption Layer)
 *
 * TODO: ProductRepository 주입받아 실제 상품명 조회 로직 구현
 */
@Component
@RequiredArgsConstructor
public class ProductQueryPortImpl implements ProductQueryPort {

    // TODO: 실제 구현 시 ProductRepository 주입
    // private final ProductRepository productRepository;

    /**
     * 상품명 조회 (정산 생성 시 스냅샷용)
     */
    @Override
    public String getProductName(Long productId) {
        // TODO: productRepository.findById(productId).map(Product::getName).orElse(null)
        return "상품-" + productId;  // 더미 데이터
    }
}