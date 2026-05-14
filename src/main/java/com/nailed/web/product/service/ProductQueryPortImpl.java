package com.nailed.web.product.service;

import com.nailed.web.product.entity.Product;
import com.nailed.web.product.repository.ProductRepository;
import com.nailed.web.settlement.service.ProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductQueryPortImpl implements ProductQueryPort {

    private final ProductRepository productRepository;

    @Override
    public String getProductName(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getTitle)
                .orElse(null);
    }
}
