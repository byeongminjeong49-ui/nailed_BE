package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    RECEIVED("Received", "Report received"),
    REVIEWING("Reviewing", "Report reviewing"),
    RESOLVED("Resolved", "Report resolved"),
    DISMISSED("Dismissed", "Report dismissed");

    private final String label;
    private final String description;
}
