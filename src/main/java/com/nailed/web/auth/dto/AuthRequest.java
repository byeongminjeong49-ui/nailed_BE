package com.nailed.web.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {

    public record Signup(
            @NotBlank(message = "Email is required.")
            @Email(message = "Email format is invalid.")
            @Size(max = 100, message = "Email must be 100 characters or less.")
            String email,
            @NotBlank(message = "Nickname is required.")
            @Size(max = 30, message = "Nickname must be 30 characters or less.")
            String nickname,
            @NotBlank(message = "Password is required.")
            @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters.")
            String password,
            @NotBlank(message = "Name is required.")
            @Size(max = 30, message = "Name must be 30 characters or less.")
            String name,
            @AssertTrue(message = "Service terms agreement is required.")
            boolean serviceTermsAgreed,
            @AssertTrue(message = "Privacy policy agreement is required.")
            boolean privacyPolicyAgreed,
            boolean marketingAgreed
    ) {}

    public record Login(
            @NotBlank(message = "Email is required.")
            @Email(message = "Email format is invalid.")
            String email,
            @NotBlank(message = "Password is required.")
            String password
    ) {}

    public record PasswordResetRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Email format is invalid.")
            String email
    ) {}
}