package com.nailed.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Member {

    @Id
    @Column(name = "member_id", length = 20)
    private String memberId;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "nickname", length = 30, nullable = false, unique = true)
    private String nickname;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "phone", length = 50, nullable = false)
    private String phone;

    @Column(name = "shop_info", length = 500)
    private String shopInfo;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "seller_grade", length = 20)
    private String sellerGrade;

    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Column(name = "depositor_name", length = 30)
    private String depositorName;

    @Column(name = "marketing_agreed", nullable = false)
    @Builder.Default
    private boolean marketingAgreed = false;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_fail_count", nullable = false)
    @Builder.Default
    private int loginFailCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private int loginCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id")
    private Member referrer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount++;
        this.loginFailCount = 0;
    }

    public void increaseLoginFailCount() {
        this.loginFailCount++;
    }

    public void lock(LocalDateTime until) {
        this.status = "LOCKED";
        this.lockedUntil = until;
    }

    public void unlock() {
        this.status = "ACTIVE";
        this.lockedUntil = null;
        this.loginFailCount = 0;
    }

    public void withdraw() {
        this.status = "WITHDRAWN";
    }

    public void updateProfile(String nickname, String phone, String shopInfo, boolean marketingAgreed) {
        this.nickname = nickname;
        this.phone = phone;
        this.shopInfo = shopInfo;
        this.marketingAgreed = marketingAgreed;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBankInfo(String bankCode, String accountNumber, String depositorName) {
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.depositorName = depositorName;
        this.updatedAt = LocalDateTime.now();
    }
}
