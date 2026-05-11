package com.nailed.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentRequest {

    public record Ready(
            @NotBlank String orderId,
            @NotBlank String method
    ) {}

    public record Approve(
            @NotBlank String orderId,
            @NotBlank String pgToken
    ) {}
}
