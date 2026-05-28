package com.nailed.web.report.repository;

import com.nailed.web.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, String> {

    // 동일 신고자가 동일 대상을 이미 신고했는지 체크 (중복 신고 차단)
    boolean existsByReporter_MemberIdAndTargetMember_MemberId(String reporterId, String targetMemberId);

    // RPT_NNN 형식 ID 중 최댓값 조회 (다음 시퀀스 번호 계산용)
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(report_id, 5) AS UNSIGNED)), 0) " +
                   "FROM reports WHERE report_id REGEXP '^RPT_[0-9]+$'", nativeQuery = true)
    Optional<Integer> findMaxSequentialNumber();
}
