package com.nailed.config.jwt;

import com.nailed.web.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE = "Bearer";

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String createAccessToken(Member member) {
        return createAccessTokenInfo(member).accessToken();
    }

    public AccessTokenInfo createAccessTokenInfo(Member member) {
        Date now = now();
        Date expiresAt = expiresAt(now, accessTokenValidityMs);

        String accessToken = Jwts.builder()
                .subject(member.getMemberId())
                .claim("userid", member.getUserid())
                .claim("role", member.getRole())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();

        return new AccessTokenInfo(
                accessToken,
                TOKEN_TYPE,
                accessTokenValidityMs / 1000,
                toLocalDateTime(expiresAt)
        );
    }

    public RefreshTokenInfo createRefreshTokenInfo(Member member) {
        Date now = now();
        Date expiresAt = expiresAt(now, refreshTokenValidityMs);

        String refreshToken = Jwts.builder()
                .subject(member.getMemberId())
                .claim("userid", member.getUserid())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();

        return new RefreshTokenInfo(
                refreshToken,
                refreshTokenValidityMs / 1000,
                toLocalDateTime(expiresAt)
        );
    }

    public boolean validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        return "access".equals(claims.get("type", String.class));
    }

    public boolean validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        return "refresh".equals(claims.get("type", String.class));
    }

    public String getMemberId(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Date now() {
        return new Date();
    }

    private Date expiresAt(Date now, long validityMs) {
        long expiresAtMs = now.getTime() + validityMs;
        return new Date((expiresAtMs / 1000) * 1000);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.SECONDS);
    }

    public record AccessTokenInfo(
            String accessToken,
            String tokenType,
            long expiresIn,
            LocalDateTime tokenExpiresAt
    ) {}

    public record RefreshTokenInfo(
            String refreshToken,
            long refreshExpiresIn,
            LocalDateTime refreshTokenExpiresAt
    ) {}
}
