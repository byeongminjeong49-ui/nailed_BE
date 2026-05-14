package com.nailed.web.product.service;

import com.nailed.common.enums.ProductStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.order.service.ProductCommandPort;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductCommandPortImpl implements ProductCommandPort {

    private final ProductRepository productRepository;

    @Override
    public ProductInfo getProductInfo(Long productId) {
        Product p = findProduct(productId);
        return new ProductInfo(p.getProductId(), p.getSellerId(), p.getPrice());
    }

    @Override
    public ProductInfo lockAndGetProductInfo(Long productId) {
        Product p = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return new ProductInfo(p.getProductId(), p.getSellerId(), p.getPrice());
    }

    @Transactional
    @Override
    public void markAsSoldOut(Long productId) {
        findProduct(productId).changeStatus(ProductStatus.SOLD_OUT);
    }

    @Transactional
    @Override
    public void restoreToOnSale(Long productId) {
        findProduct(productId).changeStatus(ProductStatus.ON_SALE);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
