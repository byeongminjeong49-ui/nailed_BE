package com.nailed.web.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 운송장 입력 요청 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingDTO {

	private String courier;        // 택배사 (CJ / 한진 / 롯데 / 우체국 등)
	private String trackingNumber; // 운송장 번호 (10~15자리)

}//class
