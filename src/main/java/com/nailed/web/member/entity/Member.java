package com.nailed.web.member.entity;

import com.nailed.common.entity.SoftDeleteEntity;
import com.nailed.common.enums.MemberStatus;
import com.nailed.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//** Member Entity
//=> 회원 테이블 매핑
//=> SoftDeleteEntity 상속 (createdAt, updatedAt, deletedAt 자동관리)
//=> claimList() : JWT 토큰 발행 시 사용 (demo 프로젝트 Member.claimList() 참고)

@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends SoftDeleteEntity {

    @Id
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 30)
    private MemberStatus memberStatus;

    //--- 생성 메서드 ------------------------------------------

    @Builder
    private Member(Long id, String email, String password, String phoneNumber,
                   Role role, MemberStatus memberStatus) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = createNickname(email);
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.memberStatus = memberStatus;
    }

    //=> 일반 회원 생성
    public static Member createUser(Long id, String email, String password, String phoneNumber) {
        return Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .role(Role.ROLE_USER)
                .memberStatus(MemberStatus.ACTIVE)
                .build();
    }

    //--- 수정 메서드 ------------------------------------------

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname.trim();
    }

    public void withdraw() {
        this.memberStatus = MemberStatus.WITHDRAWN;
        softDelete();
    }

    //--- JWT 토큰용 메서드 ------------------------------------

    //=> Access Token 발행 시 사용: id + role 포함
    //=> demo 프로젝트 Member.claimList() 참고
    public Map<String, Object> claimList() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        data.put("roleList", List.of(this.role.name().replace("ROLE_", "")));
        // => "ROLE_USER" → "USER" 로 저장
        // => JwtAuthenticationFilter 에서 다시 "ROLE_USER" 로 복원됨
        return data;
    }

    //=> Refresh Token 발행 시 사용: id 만 포함
    public Map<String, Object> refreshClaimList() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        return data;
    }

    //--- 내부 메서드 ------------------------------------------

    private static String createNickname(String email) {
        String prefix = email == null ? "user" : email.split("@")[0];
        String normalized = prefix.replaceAll("[^A-Za-z0-9가-힣]", "");
        if (normalized.isBlank()) normalized = "user";
        return normalized.length() > 30 ? normalized.substring(0, 30) : normalized;
    }

} //class
