package com.nailed.web.auth.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

    private static final int CODE_TTL_MINUTES = 10;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    public String createCode(String email) {
        String normalizedEmail = normalize(email);
        String code = String.valueOf(random.nextInt(900000) + 100000);
        verificationCodes.put(normalizedEmail, new VerificationCode(code, LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES), false));
        return code;
    }

    public void verify(String email, String code) {
        String normalizedEmail = normalize(email);
        VerificationCode saved = verificationCodes.get(normalizedEmail);

        if (saved == null || saved.expiresAt().isBefore(LocalDateTime.now()) || !saved.code().equals(code)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        verificationCodes.put(normalizedEmail, new VerificationCode(saved.code(), saved.expiresAt(), true));
    }

    public boolean isVerified(String email) {
        VerificationCode saved = verificationCodes.get(normalize(email));
        return saved != null && saved.verified() && saved.expiresAt().isAfter(LocalDateTime.now());
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record VerificationCode(String code, LocalDateTime expiresAt, boolean verified) {}
}
