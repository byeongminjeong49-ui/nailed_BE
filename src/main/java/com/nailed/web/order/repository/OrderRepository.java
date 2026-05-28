package com.nailed.web.order.repository;
import com.nailed.web.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, String> {
    // 판매자의 거래완료(DELIVERED) 건수 → 판매자 프로필 카드 표시용
    long countBySellerIdAndOrderStatus(String sellerId, String orderStatus);
    // 특정 상품의 진행중 거래 존재 여부 → 상품 삭제 불가 체크용
    long countBySellerIdAndOrderStatusIn(String sellerId, List<String> orderStatuses);
    boolean existsByProductIdAndOrderStatusIn(Long productId, List<String> statuses);
    @Modifying
    @Query(value = "UPDATE orders SET order_status = 'CANCELLED', previous_status = order_status, " +
                   "cancel_request_status = 'APPROVED', cancelled_at = NOW(), cancel_responded_at = NOW() " +
                   "WHERE order_id = :orderId", nativeQuery = true)
    void cancelOrder(@Param("orderId") String orderId);
}