package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SanctionType {
    WARNING("Warning", "Warning sanction"),
    SUSPENSION("Suspension", "Temporary suspension"),
    BAN("Ban", "Permanent ban");

    private final String label;
    private final String description;
}
