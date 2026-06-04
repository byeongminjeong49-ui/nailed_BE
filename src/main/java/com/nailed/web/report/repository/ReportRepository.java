package com.nailed.web.report.repository;

import com.nailed.common.enums.ReportReason;
import com.nailed.common.enums.ReportStatus;
import com.nailed.web.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, String> {

    boolean existsByReporter_MemberIdAndTargetMember_MemberId(String reporterId, String targetMemberId);

    // RPT_NNN 형식 ID 중 최댓값 조회 (다음 시퀀스 번호 계산용)
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(report_id, 5) AS UNSIGNED)), 0) " +
                   "FROM reports WHERE report_id REGEXP '^RPT_[0-9]+$'", nativeQuery = true)
    Optional<Integer> findMaxSequentialNumber();
    @Query(value = """
            SELECT r FROM Report r
            JOIN FETCH r.reporter reporter
            JOIN FETCH r.targetMember target
            WHERE (:keyword IS NULL
                OR LOWER(r.reportId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(reporter.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(reporter.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(target.memberId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(target.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(target.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(CAST(r.reasonCode AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.detail) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:reasonCode IS NULL OR r.reasonCode = :reasonCode)
              AND (:status IS NULL OR r.reportStatus = :status)
              AND (:dateFrom IS NULL OR r.createdAt >= :dateFrom)
              AND (:dateTo IS NULL OR r.createdAt <= :dateTo)
            """,
           countQuery = """
            SELECT COUNT(r) FROM Report r
            JOIN r.reporter reporter
            JOIN r.targetMember target
            WHERE (:keyword IS NULL
                OR LOWER(r.reportId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(reporter.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(reporter.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(target.memberId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(target.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(target.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(CAST(r.reasonCode AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.detail) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:reasonCode IS NULL OR r.reasonCode = :reasonCode)
              AND (:status IS NULL OR r.reportStatus = :status)
              AND (:dateFrom IS NULL OR r.createdAt >= :dateFrom)
              AND (:dateTo IS NULL OR r.createdAt <= :dateTo)
            """)
    Page<Report> searchAdminReports(
            @Param("keyword") String keyword,
            @Param("reasonCode") ReportReason reasonCode,
            @Param("status") ReportStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
    Page<Report> findByReporter_MemberIdOrderByCreatedAtDesc(String memberId, Pageable pageable);
}
