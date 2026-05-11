package com.nailed.domain.order.repository;

import com.nailed.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByBuyerMemberId(String buyerId, Pageable pageable);

    Page<Order> findBySellerMemberId(String sellerId, Pageable pageable);

    boolean existsByProductProductIdAndBuyerMemberId(Long productId, String buyerId);
}
