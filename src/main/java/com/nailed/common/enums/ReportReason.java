package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    FRAUD("Fraud", "Fraud report"),
    FAKE("Fake", "Fake listing report"),
    ABUSE("Abuse", "Abuse report"),
    ILLEGAL("Illegal", "Illegal content report"),
    ETC("Etc", "Etc report");

    private final String label;
    private final String description;
}
