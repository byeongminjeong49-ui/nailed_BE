package com.nailed.auth.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Log4j2
public class ResendEmailService {

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:Nailed <onboarding@resend.dev>}")
    private String fromEmail;

    public void sendLoginCode(String toEmail, String code) {
        sendEmail(toEmail, "[Nailed] Email login verification code", createCodeHtml("Nailed email login", code, 5));
    }

    public void sendSignupVerificationCode(String toEmail, String code) {
        sendEmail(toEmail, "[Nailed] 회원가입 인증번호", createCodeHtml("Nailed 회원가입 이메일 인증", code, 3));
    }

    public void sendTemporaryPassword(String toEmail, String temporaryPassword) {
        sendEmail(toEmail, "[Nailed] 임시 비밀번호 안내", createTemporaryPasswordHtml(temporaryPassword));
    }

    private void sendEmail(String toEmail, String subject, String html) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("[Resend] API key is empty. Skip email send. to={}, subject={}", toEmail, subject);
            return;
        }

        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(html)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    private String createCodeHtml(String title, String code, int minutes) {
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>%s</h2>
                    <p>Please enter the verification code below.</p>
                    <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px;">%s</p>
                    <p>This code expires in %d minutes.</p>
                </div>
                """.formatted(title, code, minutes);
    }

    private String createTemporaryPasswordHtml(String temporaryPassword) {
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Nailed temporary password</h2>
                    <p>Please sign in with the temporary password below and change it immediately.</p>
                    <p style="font-size: 24px; font-weight: bold;">%s</p>
                </div>
                """.formatted(temporaryPassword);
    }
}
