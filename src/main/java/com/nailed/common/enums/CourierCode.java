package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourierCode {

    CJ("CJ대한통운", "CJ대한통운 택배"),
    LOGEN("로젠택배", "로젠택배"),
    HANJIN("한진택배", "한진택배"),
    POST("우체국택배", "우체국택배"),
    LOTTE("롯데택배", "롯데글로벌로지스틱스");

    private final String label;
    private final String description;
}
