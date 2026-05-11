package com.nailed.domain.settlement.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.settlement.dto.SettlementResponse;
import com.nailed.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(@RequestParam String paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(settlementService.createSettlement(paymentId)));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<SettlementResponse.Detail>>> getSellerSettlements(
            @PathVariable String sellerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(settlementService.getSellerSettlements(sellerId, pageable)));
    }

    @PatchMapping("/{settlementId}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(@PathVariable String settlementId) {
        settlementService.complete(settlementId);
        return ResponseEntity.ok(ApiResponse.ok("정산이 완료 처리되었습니다.", null));
    }
}
