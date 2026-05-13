package com.nailed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailLoginRequest {

    @Email(message = "email format is invalid.")
    @NotBlank(message = "email is required.")
    private String email;
}
