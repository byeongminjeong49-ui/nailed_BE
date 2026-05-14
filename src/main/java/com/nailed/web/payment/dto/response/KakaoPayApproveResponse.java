package com.nailed.web.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoPayApproveResponse {

    private String tid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    @JsonProperty("item_name")
    private String itemName;

    private Amount amount; // 금액 검증에 사용

    @Getter
    public static class Amount {
        private Integer total;
    }

} //class
