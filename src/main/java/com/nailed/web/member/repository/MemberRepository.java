package com.nailed.web.member.repository;

import com.nailed.web.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByUserid(String userid);

    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByUserid(String userid);

    boolean existsByNickname(String nickname);

    @Modifying
    @Query("UPDATE Member m SET m.sellerGrade = :sellerGrade WHERE m.memberId = :memberId")
    int updateSellerGrade(@Param("memberId") String memberId, @Param("sellerGrade") String sellerGrade);
}
