package com.nailed.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberRequest {

    public record Join(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 20) String password,
            @NotBlank @Size(min = 2, max = 30) String nickname,
            @NotBlank String phone,
            String referrerId,
            boolean marketingAgreed
    ) {}

    public record Login(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record UpdateProfile(
            @NotBlank @Size(min = 2, max = 30) String nickname,
            @NotBlank String phone,
            String shopInfo,
            boolean marketingAgreed
    ) {}

    public record UpdateBankInfo(
            @NotBlank String bankCode,
            @NotBlank String accountNumber,
            @NotBlank String depositorName
    ) {}

    public record ChangePassword(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 20) String newPassword
    ) {}
}
