package com.nailed.web.product.dto;

public class ProductRequest {

    public record Register(
            String title,
            int price,
            String description,
            String conditionCode,
            String categoryCode,
            String imageUrl
    ) {}

    public record Update(
            String title,
            int price,
            String description,
            String conditionCode,
            String categoryCode,
            String imageUrl
    ) {}

    public record StatusUpdate(String status) {}
}
