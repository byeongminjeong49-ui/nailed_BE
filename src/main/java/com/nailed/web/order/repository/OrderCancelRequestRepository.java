package com.nailed.web.order.repository;

import com.nailed.web.order.entity.OrderCancelRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderCancelRequestRepository extends JpaRepository<OrderCancelRequest, Long> {

    Optional<OrderCancelRequest> findByOrderId(Long orderId);

    // null = 아직 응답 대기 중인 취소 요청 존재 여부
    boolean existsByOrderIdAndStatusIsNull(Long orderId);
}
