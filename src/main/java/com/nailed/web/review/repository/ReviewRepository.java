package com.nailed.web.review.repository;

import com.nailed.web.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 주문당 리뷰 중복 여부 확인
    boolean existsByOrderOrderId(String orderId);

    // 판매자 기준 리뷰 목록 (최신순)
    Page<Review> findByOrderSellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);

    // 판매자 리뷰 건수
    long countByOrderSellerId(String sellerId);

    // 판매자 평균 별점 (리뷰 없으면 empty)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.order.sellerId = :sellerId")
    Optional<Double> findAverageRatingBySellerId(@Param("sellerId") String sellerId);
}
