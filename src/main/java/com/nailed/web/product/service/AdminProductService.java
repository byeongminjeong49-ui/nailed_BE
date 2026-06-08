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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Page<Product> page = productRepository.searchAdminProducts(
                blankToNull(keyword),
                parseProductStatus(productStatus),
                categoryId,
                blankToNull(categoryCode),
                blankToNull(brandCode),
                blankToNull(brandName),
                blankToNull(sellerKeyword),
                pageable
        );
        List<Long> productIds = new ArrayList<>();
        for (Product product : page.getContent()) {
            productIds.add(product.getProductId());
        }
        Map<Long, String> thumbnailMap = buildThumbnailMap(productIds);

        return PageResponse.of(page.map(product ->
                toSummary(product, thumbnailMap.get(product.getProductId()))));
    }

    @Transactional
    public void hideProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getProductStatus() == ProductStatus.DELETED || product.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.PRODUCT_DELETED);
        }

        // @NotBlank + @Size(max=500)이 Controller @Valid 단계에서 이미 검증 → 별도 체크 불필요
        product.delete(blankToNull(reason));
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
        if (productIds.isEmpty()) return Map.of();
        List<ProductImage> thumbnails = productImageRepository.findThumbnailsByProductIds(productIds);
        Map<Long, String> map = new HashMap<>();
        for (ProductImage img : thumbnails) {
            Long pid = img.getProduct().getProductId();
            if (!map.containsKey(pid)) {
                map.put(pid, img.getImageUrl());
            }
        }
        return map;
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
