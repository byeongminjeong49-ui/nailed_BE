package com.nailed.common.config.jwt;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증
 *
 * [application.yml 설정 필요]
 * jwt:
 *   secret: "nailed-secret-key-must-be-32-chars-or-longer!!"
 *   access-token-expiration: 1800000     # 30분 (ms)
 *   refresh-token-expiration: 604800000  # 7일 (ms)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // ── 토큰 생성 ───────────────────────────────────────────

    /** Access Token 생성 (memberId + role 포함) */
    public String createAccessToken(Long memberId, String role) {
        return buildToken(memberId, role, accessTokenExpiration);
    }

    /** Refresh Token 생성 (memberId만 포함) */
    public String createRefreshToken(Long memberId) {
        return buildToken(memberId, null, refreshTokenExpiration);
    }

    private String buildToken(Long memberId, String role, long expiration) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));
        if (role != null) {
            claims.put("role", role);
        }
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ── 토큰 파싱 ───────────────────────────────────────────

    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // ── 토큰 검증 ───────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰");
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] 유효하지 않은 토큰: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
