package com.nailed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneVerificationRequest {

    @NotBlank(message = "phoneNumber is required.")
    private String phoneNumber;
}
