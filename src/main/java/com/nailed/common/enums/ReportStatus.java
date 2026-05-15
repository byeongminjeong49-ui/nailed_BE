package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {

    RECEIVED("접수", "신고가 접수된 상태"),
    REVIEWING("검토중", "신고를 검토 중인 상태"),
    RESOLVED("처리완료", "신고가 처리 완료된 상태"),
    DISMISSED("기각", "신고가 기각된 상태");

    private final String label;
    private final String description;
}
