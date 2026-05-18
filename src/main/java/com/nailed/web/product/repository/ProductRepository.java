package com.nailed.web.product.repository;

import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.web.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 삭제된 상품 제외 단건 조회 (상세 페이지)
    Optional<Product> findByProductIdAndProductStatusNot(Long productId, ProductStatus status);

    // 카테고리별 목록 (삭제 제외)
    Page<Product> findByCategoryGroupIdAndProductStatusNot(Long groupId, ProductStatus status, Pageable pageable);

    // 내 판매 상품 - 특정 상태
    Page<Product> findBySellerMemberIdAndProductStatus(String memberId, ProductStatus status, Pageable pageable);

    // 내 판매 상품 - 전체 (삭제 제외)
    Page<Product> findBySellerMemberIdAndProductStatusNot(String memberId, ProductStatus status, Pageable pageable);

    // 조회수 +1 (DB에서 직접 덧셈 → Lost Update 방지, 삭제 상품 제외)
    // 반환값: 실제 업데이트된 행 수 (0이면 존재하지 않거나 삭제된 상품)
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 " +
           "WHERE p.productId = :productId AND p.productStatus != :deleted")
    int incrementViewCount(@Param("productId") Long productId,
                           @Param("deleted") ProductStatus deleted);

    // 홈 추천: 최신 ON_SALE 6개
    List<Product> findTop6ByProductStatusOrderByCreatedAtDesc(ProductStatus status);

    // 홈 인기 TOP 10: 인기점수(조회수×1 + 찜수×3) 기준
    // 주의: 중간 매칭(%keyword%)은 인덱스 미적용 → 서비스 규모 커지면 Full-Text Search 고려
    @Query(value = "SELECT * FROM products WHERE product_status = :status " +
                   "ORDER BY (view_count + wishlist_count * 3) DESC LIMIT 10",
           nativeQuery = true)
    List<Product> findPopularTop10(@Param("status") String status);

    // 검색 + 다중 필터 (keyword / 카테고리 / 가격범위 / 상태등급 / 사이즈)
    // :param IS NULL 패턴으로 파라미터가 null이면 해당 조건을 무시
    @Query(value = "SELECT p FROM Product p WHERE p.productStatus = :onSale " +
                   "AND (:categoryId IS NULL OR p.category.groupId = :categoryId) " +
                   "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.hashtags LIKE %:keyword%) " +
                   "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                   "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                   "AND (:conditionCode IS NULL OR p.conditionCode = :conditionCode) " +
                   "AND (:size IS NULL OR p.size = :size)",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.productStatus = :onSale " +
                        "AND (:categoryId IS NULL OR p.category.groupId = :categoryId) " +
                        "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.hashtags LIKE %:keyword%) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                        "AND (:conditionCode IS NULL OR p.conditionCode = :conditionCode) " +
                        "AND (:size IS NULL OR p.size = :size)")
    Page<Product> search(@Param("onSale") ProductStatus onSale,
                         @Param("categoryId") Long categoryId,
                         @Param("keyword") String keyword,
                         @Param("minPrice") Integer minPrice,
                         @Param("maxPrice") Integer maxPrice,
                         @Param("conditionCode") ProductCondition conditionCode,
                         @Param("size") String size,
                         Pageable pageable);
}
