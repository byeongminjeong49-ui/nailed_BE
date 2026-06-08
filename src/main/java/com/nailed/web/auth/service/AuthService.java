package com.nailed.web.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.config.jwt.JwtTokenProvider;
import com.nailed.web.auth.dto.AuthRequest;
import com.nailed.web.auth.dto.AuthResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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

    // 관리자 전용 예약 키워드 (대소문자 무시)
    private static final List<String> ADMIN_RESERVED_KEYWORDS = List.of(
        // 관리자 관련
        "관리자", "관리원", "관리팀", "관리부",
        "admin", "administrator", "superadmin", "sysadmin",
        "어드민", "어드미니스트레이터",
        "MEMBER_000",
        // 운영자 관련
        "운영자", "운영팀", "운영진", "운영부", "운영원",
        "operator", "manager", "moderator",
        "오퍼레이터", "매니저", "모더레이터",
        // Nailed 브랜드 사칭
        "nailed", "네일드", "네일",
        "nailedadmin", "nailedofficial",
        // 공식/시스템 사칭
        "공식", "official", "공식계정", "공식운영",
        "시스템", "system",
        "고객센터", "고객지원", "support",
        "staff", "스태프",
        "master", "마스터",
        "root", "루트",
        // 사칭 가능성
        "총괄", "총관리", "책임자",
        "대표", "대표자",
        "임원", "직원",
        "bot", "봇"
    );

    private void validateNotAdminKeyword(String value) {
        if (value == null) return;
        String lower = value.toLowerCase().replaceAll("\\s", "");
        for (String keyword : ADMIN_RESERVED_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase().replaceAll("\\s", ""))) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }
    }
    @Transactional
    public AuthResponse.Signup signup(AuthRequest.Signup request) {
        String userid = normalizeUserid(request.userid());

        // 관리자 예약 키워드 차단
        validateNotAdminKeyword(userid);
        validateNotAdminKeyword(request.nickname());
        validateNotAdminKeyword(request.name());

        if (memberRepository.existsByUserid(userid)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // ADMIN role은 회원가입으로 취득 불가 - 무조건 USER로 고정
        Member member = Member.builder()
                .memberId(generateMemberId())
                .userid(userid)
                .passwordHash(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .name(request.name())
                .role("USER")
                .marketingAgreed(request.marketingAgreed())
                .build();

        return AuthResponse.Signup.from(memberRepository.save(member));
    }

    @Transactional(noRollbackFor = CustomException.class)
    public AuthResponse.Login login(AuthRequest.Login request, HttpServletResponse response) {
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
        memberRepository.save(member);
        System.out.println("** refresh 토큰 발급 후 update 완료->"+member);
        
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenInfo.refreshToken())
                .httpOnly(true)  //JS접근불가: document.cookie 로 읽을수없음 (XSS 공격 방어)
                .sameSite("Lax") //또는 None
                .secure(false)	 //HTTP연결 허용
                .path("/")		//모든 URL요청에 쿠키 포함
                .maxAge(Duration.ofDays(7)) //쿠키유지시간, 단위 초 (7일 설정)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        
        
        
        
        
       // return AuthResponse.Login.from(member, accessTokenInfo, refreshTokenInfo);
        return AuthResponse.Login.from(member, accessTokenInfo,null);
    }

    public AuthResponse.TokenRefresh refreshAccessToken(String refreshToken) {
        String normalized = normalizeToken(refreshToken);
        if (normalized.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findByRefreshToken(normalized)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!normalized.equals(member.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        if (member.getRefreshTokenExpiresAt() == null
                || !member.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        validateRefreshTokenFormat(normalized);
        validateMemberStatus(member);

        JwtTokenProvider.AccessTokenInfo accessTokenInfo = jwtTokenProvider.createAccessTokenInfo(member);
        return AuthResponse.TokenRefresh.from(member, accessTokenInfo);
    }

    @Transactional
    public AuthResponse.SimpleResult logout(String refreshToken, HttpServletResponse response) {
        String normalized = normalizeToken(refreshToken);
        if (!normalized.isBlank()) {
            memberRepository.findByRefreshToken(normalized)
                    .ifPresent(Member::clearRefreshToken);
        }

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

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
