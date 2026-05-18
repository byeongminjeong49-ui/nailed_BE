package com.nailed.web.product.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public class ProductRequest {

    /** 상품 등록 */
    public record Create(
            @NotBlank(message = "상품명을 입력해주세요.")
            @Size(max = 100, message = "상품명은 100자 이내로 입력해주세요.")
            String title,

            @NotNull(message = "카테고리를 선택해주세요.")
            Long categoryId,

            Long brandId,   // 선택

            @NotNull(message = "가격을 입력해주세요.")
            @Min(value = 10000, message = "최소 판매가는 10,000원입니다.")
            Integer price,

            @NotBlank(message = "상품 설명을 입력해주세요.")
            String description,

            // S / A / B / C / D
            @NotBlank(message = "상품 상태를 선택해주세요.")
            String conditionCode,

            String size,    // 선택

            @Size(max = 500, message = "해시태그는 500자 이내로 입력해주세요.")
            String hashtags,    // 쉼표 구분 (예: "#나이키,#운동화")

            // 이미지 업로드 후 반환된 URL 목록 (최소 1장, 최대 10장)
            @NotEmpty(message = "이미지를 1장 이상 등록해주세요.")
            @Size(max = 10, message = "이미지는 최대 10장까지 등록 가능합니다.")
            List<String> imageUrls
    ) {}

    /** 상품 수정 */
    public record Update(
            @NotBlank(message = "상품명을 입력해주세요.")
            @Size(max = 100, message = "상품명은 100자 이내로 입력해주세요.")
            String title,

            @NotNull(message = "카테고리를 선택해주세요.")
            Long categoryId,

            Long brandId,

            @NotNull(message = "가격을 입력해주세요.")
            @Min(value = 10000, message = "최소 판매가는 10,000원입니다.")
            Integer price,

            @NotBlank(message = "상품 설명을 입력해주세요.")
            String description,

            @NotBlank(message = "상품 상태를 선택해주세요.")
            String conditionCode,

            String size,

            @Size(max = 500, message = "해시태그는 500자 이내로 입력해주세요.")
            String hashtags,

            @NotEmpty(message = "이미지를 1장 이상 등록해주세요.")
            @Size(max = 10, message = "이미지는 최대 10장까지 등록 가능합니다.")
            List<String> imageUrls
    ) {}

    /** 판매 상태 변경 (판매자 직접 변경 가능 상태만 허용) */
    public record StatusUpdate(
            // ON_SALE / RESERVED / SOLD (DELETED는 delete API로만 처리)
            @NotBlank(message = "변경할 상태를 입력해주세요.")
            String productStatus
    ) {}
}
