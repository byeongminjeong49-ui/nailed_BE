package com.nailed.web.auth.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.config.jwt.JwtTokenProvider;
import com.nailed.web.auth.dto.AuthRequest;
import com.nailed.web.auth.dto.AuthResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String MOCK_PASSWORD_PREFIX = "{mock}";
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 10;
    private static final int LOGIN_FAIL_LIMIT = 5;
    private static final int LOGIN_FAIL_WINDOW_MINUTES = 30;
    private static final int LOGIN_LOCK_MINUTES = 10;
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

    @Transactional(noRollbackFor = CustomException.class)
    public AuthResponse.Login login(AuthRequest.Login request) {
        String userid = normalizeUserid(request.userid());
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));
        LocalDateTime now = LocalDateTime.now();

        handleExpiredLoginRestriction(member, now);
        validateMemberStatus(member);
        resetExpiredLoginFailureWindow(member, now);

        if (!matchesMockPassword(request.password(), member.getPasswordHash())) {
            recordLoginFailure(member, now);
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        member.resetLoginFailures();
        member.increaseLoginCount();
        JwtTokenProvider.AccessTokenInfo accessTokenInfo = jwtTokenProvider.createAccessTokenInfo(member);
        JwtTokenProvider.RefreshTokenInfo refreshTokenInfo = jwtTokenProvider.createRefreshTokenInfo(member);
        member.updateRefreshToken(refreshTokenInfo.refreshToken(), refreshTokenInfo.refreshTokenExpiresAt());

        return AuthResponse.Login.from(member, accessTokenInfo, refreshTokenInfo);
    }

    public AuthResponse.TokenRefresh refreshAccessToken(AuthRequest.TokenRefresh request) {
        String refreshToken = normalizeToken(request == null ? null : request.refreshToken());
        if (refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!refreshToken.equals(member.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        if (member.getRefreshTokenExpiresAt() == null
                || !member.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        validateRefreshTokenFormat(refreshToken);
        validateMemberStatus(member);

        JwtTokenProvider.AccessTokenInfo accessTokenInfo = jwtTokenProvider.createAccessTokenInfo(member);
        return AuthResponse.TokenRefresh.from(member, accessTokenInfo);
    }

    @Transactional
    public AuthResponse.SimpleResult logout(AuthRequest.TokenRefresh request) {
        String refreshToken = normalizeToken(request == null ? null : request.refreshToken());
        if (refreshToken.isBlank()) {
            return new AuthResponse.SimpleResult(true);
        }

        memberRepository.findByRefreshToken(refreshToken)
                .ifPresent(Member::clearRefreshToken);

        return new AuthResponse.SimpleResult(true);
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

    private String normalizeToken(String token) {
        return token == null ? "" : token.trim();
    }

    private void validateRefreshTokenFormat(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
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

    private void handleExpiredLoginRestriction(Member member, LocalDateTime now) {
        String status = member.getMemberStatus();
        if (!"LOCKED".equals(status) && !"SUSPEND".equals(status) && !"SUSPENDED".equals(status)) {
            return;
        }

        LocalDateTime lockedUntil = member.getLockedUntil();
        if (lockedUntil == null && !"LOCKED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_SUSPENDED);
        }
        if (lockedUntil != null && lockedUntil.isAfter(now)) {
            if ("LOCKED".equals(status)) {
                throw new CustomException(ErrorCode.MEMBER_LOCKED);
            }
            throw new CustomException(ErrorCode.MEMBER_SUSPENDED);
        }

        member.unlock();
    }

    private void resetExpiredLoginFailureWindow(Member member, LocalDateTime now) {
        LocalDateTime loginFailStartedAt = member.getLoginFailStartedAt();
        if (loginFailStartedAt == null) {
            return;
        }

        if (!loginFailStartedAt.plusMinutes(LOGIN_FAIL_WINDOW_MINUTES).isAfter(now)) {
            member.resetLoginFailures();
        }
    }

    private void recordLoginFailure(Member member, LocalDateTime now) {
        LocalDateTime startedAt = member.getLoginFailStartedAt();
        if (startedAt == null || !startedAt.plusMinutes(LOGIN_FAIL_WINDOW_MINUTES).isAfter(now)) {
            startedAt = now;
            member.recordLoginFailure(startedAt, 1);
            return;
        }

        int failCount = member.getLoginFailCount() + 1;
        if (failCount >= LOGIN_FAIL_LIMIT) {
            member.lockUntil(now.plusMinutes(LOGIN_LOCK_MINUTES));
            throw new CustomException(ErrorCode.MEMBER_LOCKED);
        }

        member.recordLoginFailure(startedAt, failCount);
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
