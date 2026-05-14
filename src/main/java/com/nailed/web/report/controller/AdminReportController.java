package com.nailed.web.report.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.report.dto.ReportRequest;
import com.nailed.web.report.dto.ReportResponse;
import com.nailed.web.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public ApiResponse<Page<ReportResponse.Item>> getAdminReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(reportService.getAdminReports(status, targetType, page, size));
    }

    @PatchMapping("/{reportId}")
    public ApiResponse<Void> processReport(@PathVariable Long reportId,
                                           @RequestBody ReportRequest.Process request) {
        reportService.processReport(reportId, request);
        return ApiResponse.success();
    }
}
