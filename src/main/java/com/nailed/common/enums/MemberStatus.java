package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    ACTIVE("Active", "Active member"),
    INACTIVE("Inactive", "Inactive member"),
    WITHDRAWN("Withdrawn", "Withdrawn member"),
    SUSPENDED("Suspended", "Suspended member"),
    BANNED("Banned", "Banned member");

    private final String label;
    private final String description;
}
