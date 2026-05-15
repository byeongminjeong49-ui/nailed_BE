package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SanctionType {

    WARNING("경고", "경고 처벌"),
    SUSPENSION("정지", "일시 정지"),
    BAN("차단", "영구 차단");

    private final String label;
    private final String description;
}
