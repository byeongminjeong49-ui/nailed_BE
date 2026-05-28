package com.nailed.web.order.repository;
import com.nailed.web.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDateTime;

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

    @Query(value = """
            SELECT o FROM Order o
            JOIN Member buyer ON buyer.memberId = o.buyerId
            JOIN Member seller ON seller.memberId = o.sellerId
            JOIN Product product ON product.productId = o.productId
            WHERE (:keyword IS NULL
                OR LOWER(o.orderId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(buyer.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(buyer.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(seller.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(seller.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(product.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus)
              AND (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
              AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            """,
           countQuery = """
            SELECT COUNT(o) FROM Order o
            JOIN Member buyer ON buyer.memberId = o.buyerId
            JOIN Member seller ON seller.memberId = o.sellerId
            JOIN Product product ON product.productId = o.productId
            WHERE (:keyword IS NULL
                OR LOWER(o.orderId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(buyer.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(buyer.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(seller.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(seller.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(product.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus)
              AND (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
              AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            """)
    Page<Order> searchAdminOrders(
            @Param("keyword") String keyword,
            @Param("orderStatus") String orderStatus,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}

