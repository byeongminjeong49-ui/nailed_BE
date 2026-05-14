package com.nailed.web.product.service;

import com.nailed.common.enums.ProductStatus;
import com.nailed.web.product.repository.ProductRepository;
import com.nailed.web.settlement.service.ProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementProductQueryPortImpl implements ProductQueryPort {

    private final ProductRepository productRepository;

    @Override
    public String getProductName(Long productId) {
        if (productId == null) {
            return null;
        }

        return productRepository.findById(productId)
                .filter(product -> product.getStatus() != ProductStatus.DELETED)
                .map(product -> product.getTitle())
                .orElse(null);
    }
}
