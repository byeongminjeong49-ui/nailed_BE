package com.nailed.web.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberProfileUpdateRequest {

    @NotBlank(message = "nickname is required.")
    @Size(max = 30, message = "nickname must be 30 characters or less.")
    private String nickname;
}
