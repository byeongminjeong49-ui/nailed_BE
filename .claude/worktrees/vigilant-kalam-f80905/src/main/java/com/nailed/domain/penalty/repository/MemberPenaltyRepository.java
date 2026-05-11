package com.nailed.domain.penalty.repository;

import com.nailed.domain.penalty.entity.MemberPenalty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPenaltyRepository extends JpaRepository<MemberPenalty, Long> {

    Page<MemberPenalty> findByMemberMemberId(String memberId, Pageable pageable);
}
