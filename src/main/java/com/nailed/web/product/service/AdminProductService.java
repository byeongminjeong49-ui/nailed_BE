package com.nailed.web.product.service;

import com.nailed.common.enums.ProductStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.product.dto.AdminProductResponse;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.entity.ProductGroup;
import com.nailed.web.product.entity.ProductImage;
import com.nailed.web.product.repository.ProductImageRepository;
import com.nailed.web.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public PageResponse<AdminProductResponse.Summary> getProducts(
            String keyword,
            String productStatus,
            Long categoryId,
            String categoryCode,
            String brandCode,
            String brandName,
            String sellerKeyword,
            Pageable pageable) {
        var page = productRepository.searchAdminProducts(
                blankToNull(keyword),
                parseProductStatus(productStatus),
                categoryId,
                blankToNull(categoryCode),
                blankToNull(brandCode),
                blankToNull(brandName),
                blankToNull(sellerKeyword),
                pageable
        );
        Map<Long, String> thumbnailMap = buildThumbnailMap(
                page.getContent().stream().map(Product::getProductId).toList());

        return PageResponse.of(page.map(product ->
                toSummary(product, thumbnailMap.get(product.getProductId()))));
    }

    private AdminProductResponse.Summary toSummary(Product product, String thumbnailUrl) {
        ProductGroup brand = product.getBrand();
        ProductGroup category = product.getCategory();
        return new AdminProductResponse.Summary(
                product.getProductId(),
                product.getTitle(),
                brand != null ? brand.getName() : null,
                category.getName(),
                buildCategoryPath(category),
                product.getPrice(),
                product.getProductStatus().name(),
                product.getViewCount(),
                product.getWishlistCount(),
                product.getSeller().getMemberId(),
                product.getSeller().getUserid(),
                product.getSeller().getNickname(),
                thumbnailUrl,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private ProductStatus parseProductStatus(String productStatus) {
        String value = blankToNull(productStatus);
        if (value == null) {
            return null;
        }
        try {
            return ProductStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Map<Long, String> buildThumbnailMap(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productImageRepository.findThumbnailsByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getProduct().getProductId(),
                        ProductImage::getImageUrl,
                        (existing, replacement) -> existing
                ));
    }

    private String buildCategoryPath(ProductGroup category) {
        List<String> parts = new ArrayList<>();
        ProductGroup current = category;
        while (current != null) {
            parts.add(0, current.getName());
            current = current.getParent();
        }
        return String.join(" > ", parts);
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
