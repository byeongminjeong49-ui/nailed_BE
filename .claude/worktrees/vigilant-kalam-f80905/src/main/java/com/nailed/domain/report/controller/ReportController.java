package com.nailed.domain.report.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.report.dto.ReportRequest;
import com.nailed.domain.report.dto.ReportResponse;
import com.nailed.domain.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> submit(
            @RequestParam String reporterId,
            @Valid @RequestBody ReportRequest.Submit request) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.submit(reporterId, request)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<ReportResponse.Detail>>> getPendingReports(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getPendingReports(pageable)));
    }

    @PatchMapping("/{reportId}/process")
    public ResponseEntity<ApiResponse<Void>> process(
            @PathVariable String reportId,
            @Valid @RequestBody ReportRequest.Process request) {
        reportService.process(reportId, request);
        return ResponseEntity.ok(ApiResponse.ok("신고가 처리되었습니다.", null));
    }
}
