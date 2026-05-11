package com.nailed.domain.settlement.repository;

import com.nailed.domain.settlement.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

    Page<Settlement> findBySellerMemberId(String sellerId, Pageable pageable);

    Page<Settlement> findByStatus(String status, Pageable pageable);
}
