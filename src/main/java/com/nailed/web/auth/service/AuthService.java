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
import lombok.RequiredArgsConstructor;

/**
 * 회원 인증 서비스
 * 클래스 레벨 readOnly=true: 기본이 조회용, 변경 메서드에만 @Transactional 재선언
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // 개발 편의용 mock 비밀번호 접두사 (운영 환경 제거 권장)
    private static final String MOCK_PASSWORD_PREFIX = "{mock}";

    // 혼동 문자(0, O, 1, I, l) 제외한 임시 비밀번호 문자셋
    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 10;

    // 로그인 실패 정책: 30분 내 5회 실패 시 10분 잠금
    private static final int LOGIN_FAIL_LIMIT          = 5;
    private static final int LOGIN_FAIL_WINDOW_MINUTES = 30;
    private static final int LOGIN_LOCK_MINUTES        = 10;

    // SecureRandom: 암호학적으로 안전한 난수 (임시 비밀번호 생성용)
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // 관리자 사칭 방지 — 아이디·닉네임·이름에 아래 키워드 포함 시 가입 불가
    private static final List<String> ADMIN_RESERVED_KEYWORDS = List.of(
        "관리자", "관리원", "관리팀", "관리부",
        "admin", "administrator", "superadmin", "sysadmin", "어드민", "어드미니스트레이터",
        "MEMBER_000",
        "운영자", "운영팀", "운영진", "운영부", "운영원",
        "operator", "manager", "moderator", "오퍼레이터", "매니저", "모더레이터",
        "nailed", "네일드", "네일", "nailedadmin", "nailedofficial",
        "공식", "official", "공식계정", "공식운영",
        "시스템", "system", "고객센터", "고객지원", "support",
        "staff", "스태프", "master", "마스터", "root", "루트",
        "총괄", "총관리", "책임자", "대표", "대표자", "임원", "직원", "bot", "봇"
    );

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ── 중복 확인 ───────────────────────────────────────────────

    public AuthResponse.DuplicateCheck checkUserid(String userid) {
        return new AuthResponse.DuplicateCheck(
                memberRepository.existsByUserid(normalizeUserid(userid)));
    }

    public AuthResponse.DuplicateCheck checkNickname(String nickname) {
        return new AuthResponse.DuplicateCheck(
                memberRepository.existsByNickname(nickname));
    }

    // ── 회원가입 ────────────────────────────────────────────────

    @Transactional
    public AuthResponse.Signup signup(AuthRequest.Signup request) {
        String userid = normalizeUserid(request.userid());

        // 관리자 사칭 키워드 검사
        validateNotAdminKeyword(userid);
        validateNotAdminKeyword(request.nickname());
        validateNotAdminKeyword(request.name());

        if (memberRepository.existsByUserid(userid))
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        if (memberRepository.existsByNickname(request.nickname()))
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);

        Member member = Member.builder()
                .memberId(generateMemberId())
                .userid(userid)
                .passwordHash(passwordEncoder.encode(request.password())) // BCrypt 암호화 저장
                .nickname(request.nickname())
                .name(request.name())
                .role("USER")
                .marketingAgreed(request.marketingAgreed())
                .build();

        return AuthResponse.Signup.from(memberRepository.save(member));
    }

    // ── 로그인 ──────────────────────────────────────────────────

    /**
     * noRollbackFor 이유: 로그인 실패 시 실패 횟수를 DB에 저장해야 하는데
     * CustomException 발생으로 롤백되면 카운트가 저장 안 됨
     */
    @Transactional(noRollbackFor = CustomException.class)
    public AuthResponse.Login login(AuthRequest.Login request, HttpServletResponse response) {
        String userid = normalizeUserid(request.userid());
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));

        LocalDateTime now = LocalDateTime.now();

        handleExpiredLoginRestriction(member, now); // 잠금·정지 기간 만료 시 자동 해제
        validateMemberStatus(member);               // 탈퇴·정지·밴·잠금 상태 검사
        resetExpiredLoginFailureWindow(member, now); // 30분 지난 실패 카운트 초기화

        if (!matchesMockPassword(request.password(), member.getPasswordHash())) {
            recordLoginFailure(member, now); // 실패 횟수 기록, 5회 시 자동 잠금
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        member.resetLoginFailures();
        member.increaseLoginCount();

        // 토큰 발급
        JwtTokenProvider.AccessTokenInfo accessTokenInfo  = jwtTokenProvider.createAccessTokenInfo(member);
        JwtTokenProvider.RefreshTokenInfo refreshTokenInfo = jwtTokenProvider.createRefreshTokenInfo(member);

        // Refresh Token DB 저장 (로그아웃 시 NULL로 무효화)
        member.updateRefreshToken(refreshTokenInfo.refreshToken(), refreshTokenInfo.refreshTokenExpiresAt());
        memberRepository.save(member);

        // Refresh Token → HttpOnly 쿠키 (JS 접근 불가 → XSS 방어)
        // secure=false는 개발 환경용, 운영 시 true로 변경
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenInfo.refreshToken())
                .httpOnly(true).sameSite("Lax").secure(false).path("/").maxAge(Duration.ofDays(7))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return AuthResponse.Login.from(member, accessTokenInfo, null);
    }

    // ── Access Token 재발급 ─────────────────────────────────────

    /** Refresh Token(HttpOnly 쿠키)으로 새 Access Token 발급 */
    public AuthResponse.TokenRefresh refreshAccessToken(String refreshToken) {
        String normalized = normalizeToken(refreshToken);
        if (normalized.isBlank()) throw new CustomException(ErrorCode.INVALID_TOKEN);

        Member member = memberRepository.findByRefreshToken(normalized)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        // DB 저장 토큰과 일치 여부 재확인 (다른 기기 로그아웃 시 불일치)
        if (!normalized.equals(member.getRefreshToken()))
            throw new CustomException(ErrorCode.INVALID_TOKEN);

        // 만료 여부 확인
        if (member.getRefreshTokenExpiresAt() == null
                || !member.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now()))
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);

        validateRefreshTokenFormat(normalized); // JWT 서명 검증
        validateMemberStatus(member);           // 정지·탈퇴 회원 재발급 불가

        return AuthResponse.TokenRefresh.from(member, jwtTokenProvider.createAccessTokenInfo(member));
    }

    // ── 로그아웃 ────────────────────────────────────────────────

    @Transactional
    public AuthResponse.SimpleResult logout(String refreshToken, HttpServletResponse response) {
        String normalized = normalizeToken(refreshToken);
        if (!normalized.isBlank()) {
            // DB의 refresh_token을 NULL로 → 해당 토큰으로 재발급 불가
            memberRepository.findByRefreshToken(normalized).ifPresent(Member::clearRefreshToken);
        }

        // 쿠키 즉시 만료 (maxAge=0)
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).sameSite("Lax").secure(false).path("/").maxAge(0).build();
        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return new AuthResponse.SimpleResult(true);
    }

    // ── 임시 비밀번호 발급 ──────────────────────────────────────

    /** 아이디로 회원 조회 후 임시 비밀번호 생성·저장 (운영 시 이메일 발송 필요) */
    @Transactional
    public AuthResponse.PasswordReset requestPasswordReset(AuthRequest.PasswordResetRequest request) {
        Member member = memberRepository.findByUserid(normalizeUserid(request.userid()))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String temporaryPassword = generateTemporaryPassword();
        member.changePasswordHash(passwordEncoder.encode(temporaryPassword));

        return new AuthResponse.PasswordReset(temporaryPassword);
    }

    // ── 내부 헬퍼 ───────────────────────────────────────────────

    /** memberId 생성: "M" + 타임스탬프, 중복 시 재시도 */
    private String generateMemberId() {
        String memberId;
        do { memberId = "M" + System.currentTimeMillis(); }
        while (memberRepository.existsById(memberId));
        return memberId;
    }

    private String normalizeUserid(String userid) { return userid == null ? "" : userid.trim(); }
    private String normalizeToken(String token)    { return token  == null ? "" : token.trim();  }

    /** Refresh Token JWT 서명 검증 — 라이브러리 예외를 CustomException으로 변환 */
    private void validateRefreshTokenFormat(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateRefreshToken(refreshToken))
                throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /** SecureRandom으로 예측 불가능한 임시 비밀번호 생성 */
    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++)
            sb.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        return sb.toString();
    }

    /**
     * 비밀번호 검증 3가지 방식
     *   1) BCrypt 해시 비교 (정상)
     *   2) {mock} 접두사 평문 비교 (개발용)
     *   3) 평문 직접 비교 (레거시 데이터 대응)
     */
    private boolean matchesMockPassword(String rawPassword, String storedPassword) {
        return passwordEncoder.matches(rawPassword, storedPassword)
                || (MOCK_PASSWORD_PREFIX + rawPassword).equals(storedPassword)
                || rawPassword.equals(storedPassword);
    }

    /**
     * 잠금·정지 기간 만료 시 자동 해제
     * lockedUntil이 과거 → unlock(), 미래 → 예외 발생
     */
    private void handleExpiredLoginRestriction(Member member, LocalDateTime now) {
        String status = member.getMemberStatus();
        if (!"LOCKED".equals(status) && !"SUSPEND".equals(status) && !"SUSPENDED".equals(status)) return;

        LocalDateTime lockedUntil = member.getLockedUntil();

        if (lockedUntil == null && !"LOCKED".equals(status))
            throw new CustomException(ErrorCode.MEMBER_SUSPENDED); // 영구 정지

        if (lockedUntil != null && lockedUntil.isAfter(now)) {
            throw new CustomException("LOCKED".equals(status)
                    ? ErrorCode.MEMBER_LOCKED : ErrorCode.MEMBER_SUSPENDED); // 아직 제한 중
        }

        member.unlock(); // 기간 만료 → ACTIVE 복구
    }

    /**
     * 실패 카운트 윈도우 만료 시 초기화
     * 예) 10:00 첫 실패 → 10:35에 재시도 시 30분 경과 → 카운트 0으로 리셋
     */
    private void resetExpiredLoginFailureWindow(Member member, LocalDateTime now) {
        LocalDateTime startedAt = member.getLoginFailStartedAt();
        if (startedAt != null && !startedAt.plusMinutes(LOGIN_FAIL_WINDOW_MINUTES).isAfter(now))
            member.resetLoginFailures();
    }

    /**
     * 로그인 실패 기록
     * 5회 도달 시 10분 잠금, 미만이면 카운트만 증가
     */
    private void recordLoginFailure(Member member, LocalDateTime now) {
        LocalDateTime startedAt = member.getLoginFailStartedAt();

        // 윈도우 없거나 만료 → 새 윈도우 시작
        if (startedAt == null || !startedAt.plusMinutes(LOGIN_FAIL_WINDOW_MINUTES).isAfter(now)) {
            member.recordLoginFailure(now, 1);
            return;
        }

        int failCount = member.getLoginFailCount() + 1;
        if (failCount >= LOGIN_FAIL_LIMIT) {
            member.lockUntil(now.plusMinutes(LOGIN_LOCK_MINUTES)); // 10분 잠금
            throw new CustomException(ErrorCode.MEMBER_LOCKED);
        }

        member.recordLoginFailure(startedAt, failCount);
    }

    /** 계정 상태 검증 — 탈퇴·정지·밴·잠금 시 예외 발생 */
    private void validateMemberStatus(Member member) {
        String status = member.getMemberStatus();
        if ("WITHDRAWN".equals(status))                              throw new CustomException(ErrorCode.MEMBER_WITHDRAWN);
        if ("SUSPEND".equals(status) || "SUSPENDED".equals(status)) throw new CustomException(ErrorCode.MEMBER_SUSPENDED);
        if ("BANNED".equals(status))                                 throw new CustomException(ErrorCode.MEMBER_BANNED);
        if ("LOCKED".equals(status))                                 throw new CustomException(ErrorCode.MEMBER_LOCKED);
    }

    /**
     * 관리자 예약 키워드 포함 여부 검사
     * 공백 제거 + 소문자 변환 후 비교 (대소문자·공백으로 우회 방지)
     */
    private void validateNotAdminKeyword(String value) {
        if (value == null) return;
        String normalized = value.toLowerCase().replaceAll("\\s", "");
        for (String keyword : ADMIN_RESERVED_KEYWORDS)
            if (normalized.contains(keyword.toLowerCase().replaceAll("\\s", "")))
                throw new CustomException(ErrorCode.FORBIDDEN);
    }
}
