package com.nailed.web.order.repository;

import com.nailed.web.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

    // 판매자의 거래완료(COMPLETED) 건수 → 판매자 프로필 카드 표시용
    long countBySellerIdAndOrderStatus(String sellerId, String orderStatus);
}
