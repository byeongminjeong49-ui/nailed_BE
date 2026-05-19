package com.nailed.web.member.repository;

import com.nailed.web.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByUserid(String userid);

    boolean existsByUserid(String userid);

    boolean existsByNickname(String nickname);
}
