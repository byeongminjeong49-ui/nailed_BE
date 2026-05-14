package com.nailed.web.product.repository;

import com.nailed.common.enums.CategoryCode;
import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.web.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);

    List<Product> findBySellerIdAndStatusNot(Long sellerId, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.status <> :excludedStatus " +
           "AND (:keyword IS NULL OR p.title LIKE %:keyword%) " +
           "AND (:categoryCode IS NULL OR p.categoryCode = :categoryCode) " +
           "AND (:conditionCode IS NULL OR p.conditionCode = :conditionCode) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(@Param("excludedStatus") ProductStatus excludedStatus,
                                 @Param("keyword") String keyword,
                                 @Param("categoryCode") CategoryCode categoryCode,
                                 @Param("conditionCode") ProductCondition conditionCode,
                                 @Param("minPrice") Integer minPrice,
                                 @Param("maxPrice") Integer maxPrice,
                                 Pageable pageable);

    List<Product> findTop6ByStatusOrderByCreatedAtDesc(ProductStatus status);

    // 조회수×1 + 찜수×3 가중치 정렬
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY (p.viewCount + p.wishlistCount * 3) DESC")
    List<Product> findTop10Popular(@Param("status") ProductStatus status, Pageable pageable);

    List<Product> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, ProductStatus status);

    List<Product> findByProductIdInAndStatusNot(List<Long> productIds, ProductStatus status);

}

