package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelRequestStatus {
    REQUESTED("Requested", "Cancel requested"),
    APPROVED("Approved", "Cancel approved"),
    REJECTED("Rejected", "Cancel rejected");

    private final String label;
    private final String description;
}
