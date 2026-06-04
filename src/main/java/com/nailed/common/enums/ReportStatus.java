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

    APPROVED("승인", "신고가 승인되어 접수된 상태"),
    REJECTED("반려", "신고가 반려된 상태"),
    DONE("완료", "신고 처리가 모두 완료된 상태");

    private final String label;
    private final String description;
}
