package com.nailed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneVerificationConfirmRequest {

    @NotBlank(message = "phoneNumber is required.")
    private String phoneNumber;

    @NotBlank(message = "verificationCode is required.")
    private String verificationCode;
}
