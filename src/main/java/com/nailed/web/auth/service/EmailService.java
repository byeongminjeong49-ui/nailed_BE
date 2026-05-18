package com.nailed.web.auth.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final String apiKey;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from-email}") String fromEmail) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void sendVerificationCode(String toEmail, String code) {
        String html = """
                <h2>Nailed 이메일 인증번호</h2>
                <p>아래 인증번호를 회원가입 화면에 입력해주세요.</p>
                <strong style="font-size:24px;">%s</strong>
                <p>인증번호는 10분 동안 유효합니다.</p>
                """.formatted(code);

        send(toEmail, "[Nailed] 이메일 인증번호", html);
    }

    public void sendPasswordResetNotice(String toEmail) {
        String html = """
                <h2>Nailed 비밀번호 찾기</h2>
                <p>비밀번호 재설정 요청이 접수되었습니다.</p>
                <p>현재 프로젝트는 발표/시연 단계라 임시 안내 메일만 발송합니다.</p>
                """;

        send(toEmail, "[Nailed] 비밀번호 재설정 안내", html);
    }

    private void send(String toEmail, String subject, String html) {
        if (!StringUtils.hasText(apiKey)) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
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
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
