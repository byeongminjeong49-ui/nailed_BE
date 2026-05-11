package com.nailed.domain.payment.repository;

import com.nailed.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByOrderOrderId(String orderId);

    boolean existsByOrderOrderId(String orderId);
}
