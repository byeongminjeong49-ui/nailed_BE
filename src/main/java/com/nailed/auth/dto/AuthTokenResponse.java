package com.nailed.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {

    private final Long memberId;
    private final String email;
    private final String accessToken;
    private final String refreshToken;
}
