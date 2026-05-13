package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourierCode {
    CJ("CJ", "CJ Logistics"),
    LOGEN("Logen", "Logen"),
    HANJIN("Hanjin", "Hanjin"),
    POST("Post", "Korea Post"),
    LOTTE("Lotte", "Lotte Global Logistics");

    private final String label;
    private final String description;
}
