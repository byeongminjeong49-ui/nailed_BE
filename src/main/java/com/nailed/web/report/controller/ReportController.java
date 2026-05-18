package com.nailed.web.report.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.report.dto.ReportRequest;
import com.nailed.web.report.dto.ReportResponse;
import com.nailed.web.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 접수 (로그인 필요)
     * POST /api/reports
     * - 상품 상세 또는 사용자 프로필 페이지에서 호출
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse.Detail>> submit(
            @Valid @RequestBody ReportRequest.Submit request) {
        String reporterId = SecurityUtil.getCurrentMemberId();
        ReportResponse.Detail result = reportService.submit(reporterId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
