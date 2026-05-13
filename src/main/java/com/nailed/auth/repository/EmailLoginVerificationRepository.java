package com.nailed.auth.repository;

import com.nailed.auth.entity.EmailLoginVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailLoginVerificationRepository extends JpaRepository<EmailLoginVerification, Long> {

    Optional<EmailLoginVerification> findTopByEmailOrderByIdDesc(String email);
}