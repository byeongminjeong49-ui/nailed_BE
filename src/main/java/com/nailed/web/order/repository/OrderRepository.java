package com.nailed.web.order.repository;

import com.nailed.common.enums.OrderStatus;
import com.nailed.web.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 거래 당사자(구매자 or 판매자)인 주문 조회
    @Query("SELECT o FROM Order o WHERE o.id = :id AND (o.buyerId = :memberId OR o.sellerId = :memberId)")
    Optional<Order> findByIdAndParty(@Param("id") Long id, @Param("memberId") Long memberId);

    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatusInOrderByCreatedAtDesc(Long buyerId, List<OrderStatus> statuses, Pageable pageable);
    Page<Order> findBySellerIdAndStatusInOrderByCreatedAtDesc(Long sellerId, List<OrderStatus> statuses, Pageable pageable);
}
