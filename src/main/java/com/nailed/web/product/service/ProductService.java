package com.nailed.web.product.service;

import com.nailed.common.enums.CategoryCode;
import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.util.EnumUtil;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.entity.Wishlist;
import com.nailed.web.product.repository.ProductRepository;
import com.nailed.web.product.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public Long register(ProductRequest.Register request) {
        Long sellerId = SecurityUtil.getCurrentMemberId();
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

    public ProductResponse.Detail getDetail(Long productId) {
        Product product = findProduct(productId);
        product.validateNotDeleted();
        return ProductResponse.Detail.from(product);
    }

    public List<ProductResponse.Summary> getListBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndStatusNot(sellerId, ProductStatus.DELETED)
                .stream().map(ProductResponse.Summary::from).toList();
    }

    public Page<ProductResponse.Card> search(String keyword, String categoryCode, String conditionCode,
                                             Integer minPrice, Integer maxPrice, int page, int size) {
        CategoryCode category = categoryCode != null ? parseCategoryCode(categoryCode) : null;
        ProductCondition condition = conditionCode != null ? parseConditionCode(conditionCode) : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.searchProducts(ProductStatus.DELETED, keyword, category, condition, minPrice, maxPrice, pageable)
                .map(ProductResponse.Card::from);
    }

    public List<ProductResponse.Card> getNewProducts() {
        return productRepository.findTop6ByStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE)
                .stream().map(ProductResponse.Card::from).toList();
    }

    public List<ProductResponse.Card> getPopularProducts() {
        return productRepository.findTop10Popular(ProductStatus.ON_SALE, PageRequest.of(0, 10))
                .stream().map(ProductResponse.Card::from).toList();
    }

    @Transactional
    public void incrementViewCount(Long productId) {
        findProduct(productId).incrementViewCount();
    }

    @Transactional
    public void addWishlist(Long productId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (wishlistRepository.existsByMemberIdAndProductId(memberId, productId)) {
            throw new CustomException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }
        Product product = findProduct(productId);
        wishlistRepository.save(new Wishlist(memberId, productId));
        product.incrementWishlistCount();
    }

    @Transactional
    public void removeWishlist(Long productId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Wishlist wishlist = wishlistRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new CustomException(ErrorCode.WISHLIST_NOT_FOUND));
        Product product = findProduct(productId);
        wishlistRepository.delete(wishlist);
        product.decrementWishlistCount();
    }

    @Transactional
    public void changeStatus(Long productId, String status) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Product product = findProduct(productId);
        validateOwner(product, memberId);
        ProductStatus newStatus = parseProductStatus(status);
        // DELETED는 delete()를 통해서만 처리 (softDelete 일관성 유지)
        if (newStatus == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        product.changeStatus(newStatus);
    }

    public List<ProductResponse.Card> getMyProducts(String status) {
        Long sellerId = SecurityUtil.getCurrentMemberId();
        if (status == null) {
            return productRepository.findBySellerIdAndStatusNot(sellerId, ProductStatus.DELETED)
                    .stream().map(ProductResponse.Card::from).toList();
        }
        return productRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, parseProductStatus(status))
                .stream().map(ProductResponse.Card::from).toList();
    }

    public List<ProductResponse.Card> getMyWishlist() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        List<Long> productIds = wishlistRepository.findByMemberId(memberId)
                .stream().map(Wishlist::getProductId).toList();
        return productRepository.findByProductIdInAndStatusNot(productIds, ProductStatus.DELETED)
                .stream().map(ProductResponse.Card::from).toList();
    }

    @Transactional
    public void update(Long productId, ProductRequest.Update request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Product product = findProduct(productId);
        validateOwner(product, memberId);
        product.update(
                request.title(),
                request.price(),
                request.description(),
                parseConditionCode(request.conditionCode()),
                parseCategoryCode(request.categoryCode()),
                request.imageUrl()
        );
    }

    @Transactional
    public void delete(Long productId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Product product = findProduct(productId);
        validateOwner(product, memberId);
        product.delete();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void validateOwner(Product product, Long memberId) {
        if (!product.getSellerId().equals(memberId)) {
            throw new CustomException(ErrorCode.PRODUCT_UNAUTHORIZED);
        }
    }

    private ProductStatus parseProductStatus(String value) {
        return EnumUtil.parse(ProductStatus.class, value, ErrorCode.INVALID_INPUT_VALUE);
    }

    private ProductCondition parseConditionCode(String value) {
        return EnumUtil.parse(ProductCondition.class, value, ErrorCode.INVALID_INPUT_VALUE);
    }

    private CategoryCode parseCategoryCode(String value) {
        return EnumUtil.parse(CategoryCode.class, value, ErrorCode.CATEGORY_NOT_FOUND);
    }
}
