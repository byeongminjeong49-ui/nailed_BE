package com.nailed.domain.report.repository;

import com.nailed.domain.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {

    Page<Report> findByStatus(String status, Pageable pageable);

    Page<Report> findByReporterMemberId(String reporterId, Pageable pageable);
}
