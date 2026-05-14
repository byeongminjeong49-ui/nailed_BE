package com.nailed.web.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CancelRequest {

    @NotBlank(message = "취소 사유를 입력해주세요.")
    private String reason;

} //class
