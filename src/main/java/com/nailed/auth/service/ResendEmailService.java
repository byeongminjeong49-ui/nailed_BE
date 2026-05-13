package com.nailed.auth.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ResendEmailService {

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:Nailed <onboarding@resend.dev>}")
    private String fromEmail;

    public void sendLoginCode(String toEmail, String code) {
        if (!StringUtils.hasText(apiKey)) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("[Nailed] Email login verification code")
                .html(createLoginCodeHtml(code))
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String createLoginCodeHtml(String code) {
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Nailed email login</h2>
                    <p>Please enter the verification code below.</p>
                    <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px;">%s</p>
                    <p>This code expires in 5 minutes.</p>
                </div>
                """.formatted(code);
    }
}