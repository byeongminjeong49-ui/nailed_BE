package com.nailed.web.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 배송 추적 응답 DTO (Mock 구현)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingDTO {

	private Long   orderId;
	private String courier;        // 택배사
	private String trackingNumber; // 운송장 번호
	private String currentStage;   // 현재 단계 (집화완료 / 이동중 / 배달완료)

	private List<TrackingStepDTO> steps; // 단계별 타임라인

}//class
