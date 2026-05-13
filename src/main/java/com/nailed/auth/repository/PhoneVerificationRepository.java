package com.nailed.auth.repository;

import com.nailed.auth.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneNumberOrderByIdDesc(String phoneNumber);

    boolean existsByPhoneNumberAndVerifiedTrue(String phoneNumber);
}
