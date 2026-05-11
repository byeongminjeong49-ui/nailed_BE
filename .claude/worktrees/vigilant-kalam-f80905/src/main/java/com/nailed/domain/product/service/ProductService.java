package com.nailed.domain.product.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import com.nailed.domain.product.dto.ProductRequest;
import com.nailed.domain.product.dto.ProductResponse;
import com.nailed.domain.product.entity.Product;
import com.nailed.domain.product.entity.ProductGroup;
import com.nailed.domain.product.entity.ProductImage;
import com.nailed.domain.product.repository.ProductGroupRepository;
import com.nailed.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long register(String sellerId, ProductRequest.Register request) {
        Member seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ProductGroup category = productGroupRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_GROUP_NOT_FOUND));

        ProductGroup brand = null;
        if (request.brandId() != null) {
            brand = productGroupRepository.findById(request.brandId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_GROUP_NOT_FOUND));
        }

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .brand(brand)
                .title(request.title())
                .price(request.price())
                .description(request.description())
                .conditionCode(request.conditionCode())
                .shippingMethod(request.shippingMethod())
                .size(request.size())
                .hashtags(request.hashtags())
                .build();

        if (request.imageUrls() != null) {
            AtomicInteger order = new AtomicInteger(0);
            request.imageUrls().forEach(url ->
                    product.getImages().add(ProductImage.builder()
                            .product(product)
                            .imageUrl(url)
                            .sortOrder(order.getAndIncrement())
                            .build()));
        }

        return productRepository.save(product).getProductId();
    }

    @Transactional
    public ProductResponse.Detail getDetail(Long productId) {
        Product product = findById(productId);
        product.increaseViewCount();
        return ProductResponse.Detail.from(product);
    }

    public Page<ProductResponse.Summary> getSellerProducts(String sellerId, Pageable pageable) {
        return productRepository.findBySellerMemberIdAndStatusNot(sellerId, "DELETED", pageable)
                .map(ProductResponse.Summary::from);
    }

    @Transactional
    public void update(Long productId, String sellerId, ProductRequest.Update request) {
        Product product = findById(productId);
        validateOwner(product, sellerId);
        product.update(request.title(), request.price(), request.description(),
                request.conditionCode(), request.shippingMethod(), request.size(), request.hashtags());
    }

    @Transactional
    public void delete(Long productId, String sellerId, ProductRequest.Delete request) {
        Product product = findById(productId);
        validateOwner(product, sellerId);
        if ("DELETED".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        product.delete(request.reason());
    }

    private Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void validateOwner(Product product, String sellerId) {
        if (!product.getSeller().getMemberId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
