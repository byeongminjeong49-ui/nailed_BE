package com.nailed.web.payment.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.payment.dto.response.KakaoPayApproveResponse;
import com.nailed.web.payment.dto.response.KakaoPayReadyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KakaoPayClient {

    private final RestTemplate restTemplate;

    @Value("${kakao.pay.secret-key}") private String secretKey;
    @Value("${kakao.pay.cid}")        private String cid;
    @Value("${kakao.pay.approval-url}") private String approvalUrl;
    @Value("${kakao.pay.cancel-url}")   private String cancelUrl;
    @Value("${kakao.pay.fail-url}")     private String failUrl;

    private static final String READY_URL   = "https://kapi.kakao.com/v1/payment/ready";
    private static final String APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
    private static final String CANCEL_URL  = "https://kapi.kakao.com/v1/payment/cancel";

    // ── 결제 준비: 카카오페이 결제창 URL 요청 ────────────────────────────────────
    public KakaoPayReadyResponse ready(Long orderId, Long buyerId, String itemName, Integer amount) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid",              cid);
        params.add("partner_order_id", String.valueOf(orderId));
        params.add("partner_user_id",  String.valueOf(buyerId));
        params.add("item_name",        itemName);
        params.add("quantity",         "1");
        params.add("total_amount",     String.valueOf(amount));
        params.add("tax_free_amount",  "0");
        params.add("approval_url",     approvalUrl + "?orderId=" + orderId);
        params.add("cancel_url",       cancelUrl   + "?orderId=" + orderId);
        params.add("fail_url",         failUrl     + "?orderId=" + orderId);

        try {
            ResponseEntity<KakaoPayReadyResponse> response =
                    restTemplate.postForEntity(READY_URL, new HttpEntity<>(params, makeHeaders()), KakaoPayReadyResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    // ── 결제 승인: 사용자 결제 완료 후 최종 승인 ─────────────────────────────────
    public KakaoPayApproveResponse approve(Long orderId, Long buyerId, String tid, String pgToken) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid",              cid);
        params.add("tid",              tid);
        params.add("partner_order_id", String.valueOf(orderId));
        params.add("partner_user_id",  String.valueOf(buyerId));
        params.add("pg_token",         pgToken);

        try {
            ResponseEntity<KakaoPayApproveResponse> response =
                    restTemplate.postForEntity(APPROVE_URL, new HttpEntity<>(params, makeHeaders()), KakaoPayApproveResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 결제 취소(환불) - 취소 수락 시 호출 (IA nld-606 명세)
     *
     * @param tid           카카오페이 거래 번호
     * @param cancelAmount  환불 금액 (전액 환불)
     */
    public void cancel(String tid, Integer cancelAmount) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid",                   cid);
        params.add("tid",                   tid);
        params.add("cancel_amount",         String.valueOf(cancelAmount));
        params.add("cancel_tax_free_amount", "0");

        try {
            restTemplate.postForEntity(CANCEL_URL, new HttpEntity<>(params, makeHeaders()), Void.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_REFUND_FAILED);
        }
    }

    private HttpHeaders makeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", secretKey);
        headers.add("Content-Type",  "application/x-www-form-urlencoded;charset=utf-8");
        return headers;
    }
}
