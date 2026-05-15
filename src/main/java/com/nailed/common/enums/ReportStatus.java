package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신고 처리 상태
 * DB: reports.report_status VARCHAR(20) DEFAULT 'PENDING'
 * 주석값: PENDING/APPROVED/REJECTED/DONE
 */
@Getter
@RequiredArgsConstructor
public enum ReportStatus {

    PENDING("처리대기", "신고가 접수되어 처리 대기 중인 상태"),
    APPROVED("승인", "신고가 승인되어 제재 처리된 상태"),
    REJECTED("반려", "신고가 반려된 상태"),
    DONE("완료", "신고 처리가 모두 완료된 상태");

    private final String label;
    private final String description;
}
