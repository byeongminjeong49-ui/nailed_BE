package com.nailed.web.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 거래 상태 이력 DTO (Stepper UI용)
// IA: orders 단일 테이블 기반, 별도 이력 테이블 없음
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryDTO {

	private String        status;      // 상태 코드
	private String        statusLabel; // 한글 상태명
	private LocalDateTime time;        // 해당 상태 진입 시각

}//class
