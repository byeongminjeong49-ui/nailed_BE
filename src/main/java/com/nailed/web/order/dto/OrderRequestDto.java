package com.nailed.web.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor
public class OrderRequestDto {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @NotNull(message = "상품 금액은 필수입니다.")
    @Min(value = 0, message = "상품 금액은 0 이상이어야 합니다.")
    private Integer productAmount;

    @NotNull(message = "배송비는 필수입니다.")
    @Min(value = 0, message = "배송비는 0 이상이어야 합니다.")
    private Integer shippingFee;

    @NotBlank(message = "수령자 이름은 필수입니다.")
    @Size(max = 30)
    private String receiverName;

    @NotBlank(message = "수령자 연락처는 필수입니다.")
    @Size(max = 50)
    private String receiverPhone;

    @NotBlank(message = "우편번호는 필수입니다.")
    @Size(max = 10)
    private String receiverZipcode;

    @NotBlank(message = "배송지 주소는 필수입니다.")
    @Size(max = 200)
    private String receiverAddress;

    @Size(max = 100)
    private String receiverAddressDetail;

    @Size(max = 255)
    private String deliveryRequest;

    public int calcFinalPrice() {
        return productAmount + shippingFee;
    }

    public int calcSettlementAmount(int commissionRate) {
        int finalPrice = calcFinalPrice();
        return finalPrice - ((finalPrice * commissionRate) / 100);
    }
}
