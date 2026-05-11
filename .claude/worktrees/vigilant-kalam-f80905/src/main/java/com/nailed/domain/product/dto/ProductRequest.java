package com.nailed.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProductRequest {

    public record Register(
            @NotNull Long categoryId,
            Long brandId,
            @NotBlank String title,
            @NotNull @Min(0) int price,
            @NotBlank String description,
            @NotBlank String conditionCode,
            @NotBlank String shippingMethod,
            String size,
            String hashtags,
            List<String> imageUrls
    ) {}

    public record Update(
            @NotBlank String title,
            @NotNull @Min(0) int price,
            @NotBlank String description,
            @NotBlank String conditionCode,
            @NotBlank String shippingMethod,
            String size,
            String hashtags
    ) {}

    public record Delete(String reason) {}
}
