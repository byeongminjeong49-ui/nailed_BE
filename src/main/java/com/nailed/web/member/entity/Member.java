package com.nailed.web.member.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 엔티티 (최소 정의)
 * - Product / Report / Review 에서 FK 참조 및 판매자 정보 표시에 사용
 * - 전체 회원 기능(가입·로그인·탈퇴 등)은 member 도메인 구현 시 확장 예정
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id", length = 20)
    private String memberId;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "nickname", length = 30, nullable = false, unique = true)
    private String nickname;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "shop_info", length = 500)
    private String shopInfo;

    // 계정 상태 (ACTIVE / LOCKED / WITHDRAWN / SUSPEND / BANNED)
    @Column(name = "member_status", length = 20, nullable = false)
    @Builder.Default
    private String memberStatus = "ACTIVE";

    // 판매자 등급 (BRONZE / SILVER / GOLD / DIAMOND)
    @Column(name = "seller_grade", length = 20, nullable = false)
    @Builder.Default
    private String sellerGrade = "BRONZE";

    // 권한 (USER / ADMIN)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(name = "marketing_agreed", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean marketingAgreed = false;

    @Column(name = "login_fail_count", nullable = false)
    @Builder.Default
    private int loginFailCount = 0;

    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private int loginCount = 0;
}