package com.nailed.web.report.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.report.dto.ReportRequest;
import com.nailed.web.report.dto.ReportResponse;
import com.nailed.web.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse.Detail>> submit(
            @Valid @RequestBody ReportRequest.Submit request) {
        String reporterId = SecurityUtil.getCurrentMemberId();
        ReportResponse.Detail result = reportService.submit(reporterId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<ReportResponse.Summary>>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String memberId = SecurityUtil.getCurrentMemberId();
        Page<ReportResponse.Summary> result = reportService.getMyReports(
                memberId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}