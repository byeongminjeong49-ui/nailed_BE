package com.nailed.web.report.repository;

import com.nailed.web.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, String> {

    // RPT_001 형태 시퀀스 생성을 위한 현재 최대 번호 조회
    // 예: RPT_003 → 3 반환, 없으면 0 반환
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(report_id, 5) AS UNSIGNED)), 0) FROM reports",
           nativeQuery = true)
    long findMaxSequence();
}
