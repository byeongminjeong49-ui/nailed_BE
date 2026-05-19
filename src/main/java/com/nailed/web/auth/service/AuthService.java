package com.nailed.web.auth.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.config.jwt.JwtTokenProvider;
import com.nailed.web.auth.dto.AuthRequest;
import com.nailed.web.auth.dto.AuthResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String MOCK_PASSWORD_PREFIX = "{mock}";
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
   

    public AuthResponse.DuplicateCheck checkUserid(String userid) {
        return new AuthResponse.DuplicateCheck(memberRepository.existsByUserid(normalizeUserid(userid)));
    }

    public AuthResponse.DuplicateCheck checkNickname(String nickname) {
        return new AuthResponse.DuplicateCheck(memberRepository.existsByNickname(nickname));
    }

    @Transactional
    public AuthResponse.Signup signup(AuthRequest.Signup request) {
        String userid = normalizeUserid(request.userid());

        if (memberRepository.existsByUserid(userid)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        

        Member member = Member.builder()
                .memberId(generateMemberId())
                .userid(userid)
                .passwordHash(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .name(request.name())
                .marketingAgreed(request.marketingAgreed())
                .build();

        return AuthResponse.Signup.from(memberRepository.save(member));
    }

    public AuthResponse.Login login(AuthRequest.Login request) {
        String userid = normalizeUserid(request.userid());
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));

        validateMemberStatus(member);

        if (!matchesMockPassword(request.password(), member.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        return AuthResponse.Login.from(member, jwtTokenProvider.createAccessToken(member));
    }

  

    @Transactional
    public AuthResponse.PasswordReset requestPasswordReset(AuthRequest.PasswordResetRequest request) {
        String userid = normalizeUserid(request.userid());
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String temporaryPassword = generateTemporaryPassword();
        member.changePasswordHash(passwordEncoder.encode(temporaryPassword));

        return new AuthResponse.PasswordReset(temporaryPassword);
    }

    private String generateMemberId() {
        String memberId;
        do {
            memberId = "M" + System.currentTimeMillis();
        } while (memberRepository.existsById(memberId));
        return memberId;
    }

    private String normalizeUserid(String userid) {
        return userid == null ? "" : userid.trim();
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    private boolean matchesMockPassword(String rawPassword, String storedPassword) {
        return passwordEncoder.matches(rawPassword, storedPassword)
                || (MOCK_PASSWORD_PREFIX + rawPassword).equals(storedPassword)
                || rawPassword.equals(storedPassword);
    }

    private void validateMemberStatus(Member member) {
        String status = member.getMemberStatus();

        if ("WITHDRAWN".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_WITHDRAWN);
        }
        if ("SUSPEND".equals(status) || "SUSPENDED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_SUSPENDED);
        }
        if ("BANNED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_BANNED);
        }
        if ("LOCKED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_LOCKED);
        }
    }
}
