package com.nailed.web.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

} //class
