package com.nailed.web.product.service;

import com.nailed.common.enums.ProductStatus;
import com.nailed.web.order.service.ProductCommandPort;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderProductCommandPortImpl implements ProductCommandPort {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductInfo getProductInfo(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getStatus() != ProductStatus.DELETED)
                .map(this::toProductInfo)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInfo lockAndGetProductInfo(Long productId) {
        return getProductInfo(productId);
    }

    @Override
    @Transactional
    public void markAsSoldOut(Long productId) {
        productRepository.findById(productId)
                .ifPresent(Product::markAsSoldOut);
    }

    @Override
    @Transactional
    public void restoreToOnSale(Long productId) {
        productRepository.findById(productId)
                .ifPresent(Product::restoreToOnSale);
    }

    private ProductInfo toProductInfo(Product product) {
        return new ProductInfo(
                product.getProductId(),
                Long.valueOf(product.getSellerId()),
                product.getPrice()
        );
    }
}
