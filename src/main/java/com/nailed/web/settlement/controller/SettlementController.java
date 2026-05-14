package com.nailed.web.settlement.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.common.response.PageResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.settlement.dto.request.AdminCompleteRequest;
import com.nailed.web.settlement.dto.response.AdminSettlementResponse;
import com.nailed.web.settlement.dto.response.SettlementResponse;
import com.nailed.web.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // 1. 마이페이지 - 내 정산 내역 조회 (판매자)
    @GetMapping("/api/members/me/settlements")
    public ResponseEntity<ApiResponse<PageResponse<SettlementResponse>>> getMySettlements(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Long sellerId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(settlementService.getMySettlements(sellerId, status, pageable))));
    }

    // 2. 관리자 - 전체 정산 목록 조회 (다중 조건 필터)
    @GetMapping("/admin/settlements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminSettlementResponse>>> getSettlementsForAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(settlementService.getSettlementsForAdmin(status, sellerId, from, to, pageable))));
    }

    // 3. 관리자 - 단건 정산 완료 처리
    @PatchMapping("/admin/settlements/{settlementId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> completeSettlement(@PathVariable Long settlementId) {
        settlementService.completeSettlement(settlementId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 4. 관리자 - 다건 정산 일괄 완료 처리
    @PatchMapping("/admin/settlements/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> completeSettlements(@Valid @RequestBody AdminCompleteRequest request) {
        settlementService.completeSettlements(request.getSettlementIds());
        return ResponseEntity.ok(ApiResponse.success());
    }

} //class
