package com.nailed.web.product.service;

import com.nailed.common.enums.CategoryCode;
import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 등록
    @Transactional
    public Long register(ProductRequest.Register request) {
        String sellerId = String.valueOf(SecurityUtil.getCurrentMemberId());
        Product product = new Product(
                sellerId,
                request.title(),
                request.price(),
                request.description(),
                parseConditionCode(request.conditionCode()),
                parseCategoryCode(request.categoryCode()),
                request.imageUrl()
        );
        return productRepository.save(product).getProductId();
    }

    // 상품 상세 조회
    public ProductResponse.Detail getDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.PRODUCT_DELETED);
        }
        return ProductResponse.Detail.from(product);
    }

    // 판매자 상품 목록 조회
    public List<ProductResponse.Summary> getListBySeller(String sellerId) {
        List<Product> products = productRepository.findBySellerIdAndStatusNot(sellerId, ProductStatus.DELETED);
        return products.stream()
                .map(ProductResponse.Summary::from)
                .toList();
    }

    // 상품 수정
    @Transactional
    public void update(Long productId, ProductRequest.Update request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.update(
                request.title(),
                request.price(),
                request.description(),
                parseConditionCode(request.conditionCode()),
                parseCategoryCode(request.categoryCode()),
                request.imageUrl()
        );
    }

    // 상품 삭제
    @Transactional
    public void delete(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.delete();
    }

    private ProductCondition parseConditionCode(String value) {
        try {
            return ProductCondition.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private CategoryCode parseCategoryCode(String value) {
        try {
            return CategoryCode.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }
}
