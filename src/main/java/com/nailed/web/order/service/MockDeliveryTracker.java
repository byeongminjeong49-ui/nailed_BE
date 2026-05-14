package com.nailed.web.order.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.order.dto.response.TrackingResponse;
import com.nailed.web.order.entity.Delivery;
import com.nailed.web.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock 배송 추적기 - DeliveryTracker 인터페이스 구현체 (IA nld-604 명세)
 *
 * 운송장 등록 시각 기준 경과 시간에 따라 배송 단계를 자동 진행:
 *   1분 → 집하 완료
 *   2분 → 배송 중
 *   5분 → 간선 하차
 *  10분 → 배송 완료 + Order 상태 DELIVERED 자동 변경
 *
 * 에러 분기:
 *   - 운송장 미등록         → DELIVERY_NOT_FOUND (404)
 *   - 시스템 장애(예외 발생) → DELIVERY_SYSTEM_ERROR (503)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockDeliveryTracker implements DeliveryTracker {

    private final OrderRepository orderRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional
    public TrackingResponse track(Delivery delivery) {
        try {
            return buildTrackingResponse(delivery);
        } catch (CustomException ce) {
            throw ce; // 의도된 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            // 예상치 못한 시스템 장애 → 503 (IA nld-604 명세)
            log.error("배송 추적 시스템 오류: orderId={}", delivery.getOrderId(), e);
            throw new CustomException(ErrorCode.DELIVERY_SYSTEM_ERROR);
        }
    }

    private TrackingResponse buildTrackingResponse(Delivery delivery) {

        LocalDateTime registeredAt = (delivery.getCreatedAt() != null)
                ? delivery.getCreatedAt() : LocalDateTime.now();

        long minutesElapsed = Duration.between(registeredAt, LocalDateTime.now()).toMinutes();

        List<TrackingResponse.TrackingDetail> details = new ArrayList<>();
        String currentStatus = "배송 준비 중";

        // 1분: 집하 완료
        details.add(new TrackingResponse.TrackingDetail(
                "집하 완료", "용인 Hub", registeredAt.plusMinutes(1).format(FORMATTER)));

        // 2분: 배송 중
        if (minutesElapsed >= 2) {
            details.add(new TrackingResponse.TrackingDetail(
                    "배송 중", "옥천 Hub", registeredAt.plusMinutes(2).format(FORMATTER)));
            currentStatus = "배송 중";
        }

        // 5분: 간선 하차
        if (minutesElapsed >= 5) {
            details.add(new TrackingResponse.TrackingDetail(
                    "간선 하차", "성남 분당 대리점", registeredAt.plusMinutes(5).format(FORMATTER)));
            currentStatus = "배송 중";
        }

        // 10분: 배송 완료 + Order 상태 DELIVERED 자동 변경
        if (minutesElapsed >= 10) {
            details.add(new TrackingResponse.TrackingDetail(
                    "배송 완료", "수령인 자택", registeredAt.plusMinutes(10).format(FORMATTER)));
            currentStatus = "배송 완료";

            orderRepository.findById(delivery.getOrderId()).ifPresent(order -> {
                if (order.getStatus() == OrderStatus.SHIPPING) {
                    order.deliver();
                }
            });
        }

        Collections.reverse(details); // 최신 단계가 위에 오도록 역순

        return TrackingResponse.builder()
                .trackingNumber(delivery.getTrackingNumber())
                .carrier("CJ대한통운 (Mock)")
                .status(currentStatus)
                .details(details)
                .build();
    }
}
