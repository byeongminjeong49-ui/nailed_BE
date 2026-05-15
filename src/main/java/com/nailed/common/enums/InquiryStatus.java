package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {

    RECEIVED("접수", "문의가 접수된 상태"),
    IN_PROGRESS("처리중", "문의를 처리하고 있는 상태"),
    RESOLVED("완료", "문의가 해결된 상태");

    private final String label;
    private final String description;
}
