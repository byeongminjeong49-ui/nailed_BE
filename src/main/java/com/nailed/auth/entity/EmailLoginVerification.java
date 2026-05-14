package com.nailed.auth.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "email_login_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLoginVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_login_verification_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String verificationCode;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private EmailLoginVerification(String email, String verificationCode, LocalDateTime expiresAt) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    public static EmailLoginVerification issue(String email, String verificationCode) {
        return issue(email, verificationCode, 5);
    }

    public static EmailLoginVerification issue(String email, String verificationCode, int expiresInMinutes) {
        return EmailLoginVerification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .expiresAt(LocalDateTime.now().plusMinutes(expiresInMinutes))
                .build();
    }

    public boolean canVerify(String code) {
        return !verified
                && verificationCode.equals(code)
                && expiresAt.isAfter(LocalDateTime.now());
    }

    public void complete() {
        this.verified = true;
    }

    public boolean isVerifiedAndValid() {
        return verified && expiresAt.isAfter(LocalDateTime.now());
    }
}
