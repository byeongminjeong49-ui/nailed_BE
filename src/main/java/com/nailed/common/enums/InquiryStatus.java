package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {
    RECEIVED("Received", "Inquiry received"),
    IN_PROGRESS("In progress", "Inquiry in progress"),
    RESOLVED("Resolved", "Inquiry resolved");

    private final String label;
    private final String description;
}
