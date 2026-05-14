package com.nailed.web.order.dto.request;

import com.nailed.common.enums.CourierCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ShippingRequest {

    @NotNull(message = "택배사를 선택해주세요.")
    private CourierCode courierCode;

    // 운송장 번호: 10~15자리 숫자
    @NotBlank(message = "운송장 번호를 입력해주세요.")
    @Pattern(regexp = "\\d{10,15}", message = "운송장 번호는 10~15자리 숫자입니다.")
    private String trackingNumber;

} //class
