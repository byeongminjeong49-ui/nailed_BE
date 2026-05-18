package com.nailed.web.order.service;

import com.nailed.web.order.dto.OrderResponseDto;

public interface ShippingService {

    // 운송장 등록 (판매자가 택배사 + 운송장 번호 입력)
    OrderResponseDto registerTracking(String orderId, String carrierCode, String trackingNumber);

    // 배송 완료 처리 (mock: 즉시 완료 처리)
    OrderResponseDto confirmDelivery(String orderId);
}
