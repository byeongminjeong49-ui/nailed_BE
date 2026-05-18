package com.nailed.web.report.repository;

import com.nailed.web.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {

    // 동일 신고자가 동일 대상을 이미 신고했는지 체크 (중복 신고 차단)
    boolean existsByReporter_MemberIdAndTargetMember_MemberId(String reporterId, String targetMemberId);
}
