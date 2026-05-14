package com.nailed.web.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoPayReadyResponse {

    private String tid;

    @JsonProperty("next_redirect_pc_url")
    private String nextRedirectPcUrl;

    @JsonProperty("next_redirect_mobile_url")
    private String nextRedirectMobileUrl;

} //class
