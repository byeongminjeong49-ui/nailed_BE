package com.nailed.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleAlertResponse {

    private final String alertMessage;
    private final String value;
}
