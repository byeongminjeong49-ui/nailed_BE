package com.nailed.web.order.repository;

import com.nailed.web.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    // 판매자의 거래완료(DELIVERED) 건수 → 판매자 프로필 카드 표시용
    long countBySellerIdAndOrderStatus(String sellerId, String orderStatus);

    // 특정 상품의 진행중 거래 존재 여부 → 상품 삭제 불가 체크용
    boolean existsByProductIdAndOrderStatusIn(Long productId, List<String> statuses);
}
