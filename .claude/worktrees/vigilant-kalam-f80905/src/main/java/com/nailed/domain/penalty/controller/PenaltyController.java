package com.nailed.domain.penalty.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.penalty.dto.PenaltyRequest;
import com.nailed.domain.penalty.dto.PenaltyResponse;
import com.nailed.domain.penalty.service.PenaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> impose(@Valid @RequestBody PenaltyRequest.Impose request) {
        return ResponseEntity.ok(ApiResponse.ok(penaltyService.impose(request)));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<PenaltyResponse.Detail>>> getMemberPenalties(
            @PathVariable String memberId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(penaltyService.getMemberPenalties(memberId, pageable)));
    }
}
