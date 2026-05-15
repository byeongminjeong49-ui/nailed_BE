package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신고 사유 코드
 * DB: reports.reason_code VARCHAR(30)
 * 주석값: FRAUD/ABUSE/PROHIBITED_ITEM/ETC 등
 */
@Getter
@RequiredArgsConstructor
public enum ReportReason {

    FRAUD("사기", "사기 의심 거래"),
    ABUSE("욕설/비방", "욕설, 비방, 부적절한 언행"),
    PROHIBITED_ITEM("금지상품", "거래 금지 품목"),
    ETC("기타", "기타 신고 사유");

    private final String label;
    private final String description;
}
