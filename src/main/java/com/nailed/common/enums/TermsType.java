package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsType {
    SERVICE("Service", "Service terms"),
    PRIVACY("Privacy", "Privacy terms"),
    MARKETING("Marketing", "Marketing terms");

    private final String label;
    private final String description;
}
