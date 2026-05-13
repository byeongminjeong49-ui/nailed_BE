package com.nailed.web.order.repository;

import com.nailed.web.order.entity.Order;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

	//** 구매자의 주문 목록 (최신순)
	Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

	//** 판매자의 주문 목록 (최신순)
	Page<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

	//** 구매자 주문 + 상태 필터
	Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, String status, Pageable pageable);

	//** 판매자 주문 + 상태 필터
	Page<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status, Pageable pageable);

	//** 동시 구매 방지 - 비관적 락
	//=> 같은 상품에 진행 중인 주문이 있는지 확인 (DB 락을 걸어 동시 요청 차단)
	//=> lock.timeout=0 : 락 획득 실패 시 대기 없이 즉시 예외 발생 → 409 반환
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
	@Query("SELECT o FROM Order o WHERE o.productId = :productId " +
	       "AND o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')")
	Optional<Order> findActiveOrderByProductIdWithLock(@Param("productId") Long productId);

}//repository
