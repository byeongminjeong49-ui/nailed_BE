package com.nailed.web.product.dto;

public class ProductRequest {

    // 상품 등록할 때 받는 값
    public record Register(
            String title,
            int price,
            String description,
            String conditionCode,
            String categoryCode,
            String imageUrl
    ) {}

    // 상품 수정할 때 받는 값
    public record Update(
            String title,
            int price,
            String description,
            String conditionCode,
            String categoryCode,
            String imageUrl
    ) {}
}
