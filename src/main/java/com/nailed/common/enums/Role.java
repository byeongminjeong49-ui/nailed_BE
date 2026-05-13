package com.nailed.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_USER("User", "Default member role"),
    ROLE_ADMIN("Admin", "Admin role");

    private final String label;
    private final String description;
}
