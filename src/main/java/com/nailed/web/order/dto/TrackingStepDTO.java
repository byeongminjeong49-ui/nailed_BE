package com.nailed.web.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 배송 추적 단계별 정보
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingStepDTO {

	private String        stage;       // 집화완료 / 이동중 / 배달완료
	private String        description; // 단계 설명
	private LocalDateTime time;        // 해당 단계 도달 시각 (미도달 시 null)
	private boolean       done;        // 완료 여부

}//class
