package com.nailed.web.settlement.repository;

import com.nailed.web.settlement.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // 중복 정산 방지 (createSettlement 멱등성)
    boolean existsByOrderId(Long orderId);

    // 거래 취소 연동 정산 취소용
    Optional<Settlement> findByOrderId(Long orderId);

    // 판매자 정산 내역 전체 조회
    Page<Settlement> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    // 판매자 정산 내역 + 상태 필터 조회
    Page<Settlement> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status, Pageable pageable);

    // 관리자 다중 조건 필터 조회 (null 조건은 무시)
    @Query("SELECT s FROM Settlement s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:sellerId IS NULL OR s.sellerId = :sellerId) AND " +
           "(:from IS NULL OR s.createdAt >= :from) AND " +
           "(:to IS NULL OR s.createdAt <= :to) " +
           "ORDER BY s.createdAt DESC")
    Page<Settlement> findByAdminFilter(
            @Param("status")   String status,
            @Param("sellerId") Long sellerId,
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            Pageable pageable);
}
