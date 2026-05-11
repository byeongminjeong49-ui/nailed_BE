package com.nailed.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    public record Place(
            @NotNull Long productId,
            @NotBlank String receiverName,
            @NotBlank String receiverPhone,
            @NotBlank String receiverZipcode,
            @NotBlank String receiverAddress,
            String receiverAddressDetail
    ) {}

    public record Ship(
            @NotBlank String carrierCode,
            @NotBlank String trackingNumber
    ) {}

    public record CancelRequest(String reason) {}

    public record CancelResponse(boolean accept) {}
}
