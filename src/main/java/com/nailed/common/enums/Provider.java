package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    LOGIN("Login", "Email login"),
    LOGOUT("Logout", "Logout");

    private final String label;
    private final String description;
}
