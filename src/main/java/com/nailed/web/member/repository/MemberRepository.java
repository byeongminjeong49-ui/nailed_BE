package com.nailed.web.member.repository;

import com.nailed.web.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Member> findByRefreshToken(String refreshToken);

    @Query("select coalesce(max(m.id), 0) from Member m")
    Long findMaxId();
}
