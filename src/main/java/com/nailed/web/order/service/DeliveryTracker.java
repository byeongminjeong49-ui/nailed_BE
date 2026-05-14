package com.nailed.web.order.service;

import com.nailed.web.order.dto.response.TrackingResponse;
import com.nailed.web.order.entity.Delivery;

/**
 * 배송 추적 어댑터 인터페이스 (IA nld-604 명세: 어댑터 패턴)
 *
 * 기본 구현체: MockDeliveryTracker
 * 실 서비스 전환 시 이 인터페이스를 구현한 RealDeliveryTracker 를 Bean 으로 교체
 */
public interface DeliveryTracker {
    TrackingResponse track(Delivery delivery);
}
