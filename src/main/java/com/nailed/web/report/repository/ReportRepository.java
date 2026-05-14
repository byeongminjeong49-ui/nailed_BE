package com.nailed.web.report.repository;

import com.nailed.common.enums.ReportStatus;
import com.nailed.common.enums.ReportTargetType;
import com.nailed.web.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:targetType IS NULL OR r.targetType = :targetType)")
    Page<Report> findFiltered(@Param("status") ReportStatus status,
                              @Param("targetType") ReportTargetType targetType,
                              Pageable pageable);
}
