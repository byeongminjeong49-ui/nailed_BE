package com.nailed.web.report.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.report.dto.ReportRequest;
import com.nailed.web.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ApiResponse<Long> createReport(@RequestBody ReportRequest.Create request) {
        return ApiResponse.success(reportService.createReport(request));
    }
}
