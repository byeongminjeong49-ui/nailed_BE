package com.nailed.web.member.repository;

import com.nailed.web.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {

    boolean existsByNickname(String nickname);
}
