package com.nailed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailLoginConfirmRequest {

    @Email(message = "email format is invalid.")
    @NotBlank(message = "email is required.")
    private String email;

    @Pattern(regexp = "^[0-9]{6}$", message = "verificationCode must be 6 digits.")
    @NotBlank(message = "verificationCode is required.")
    private String verificationCode;
}