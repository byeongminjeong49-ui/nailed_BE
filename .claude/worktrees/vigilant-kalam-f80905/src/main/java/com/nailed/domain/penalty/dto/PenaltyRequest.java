package com.nailed.domain.penalty.dto;

import jakarta.validation.constraints.NotBlank;

public class PenaltyRequest {

    public record Impose(
            @NotBlank String memberId,
            @NotBlank String penaltyType,
            Integer penaltyDays,
            @NotBlank String reason,
            String reportId
    ) {}
}
