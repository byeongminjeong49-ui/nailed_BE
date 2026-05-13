package com.nailed.common.config.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

//** TokenProvider
//=> JWT 토큰 생성 & 검증
//=> demo 프로젝트 TokenProvider 참고

@Service
public class TokenProvider {

    //=> 암호키: 길이 30 이상 권장
    private static final String SECRET_KEY = "nailed-secret-key-must-be-32-chars-or-longer!!";

    //1. Token 생성
    //=> claimList : id, role 등 토큰에 담을 정보 (Member.claimList() 참고)
    //=> min       : 만료시간 (분 단위, 예: 24*60 = 1일)
    public String createToken(Map<String, Object> claimList, int min) {
        SecretKey key;
        try {
            key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(claimList)
                .setIssuer("nailed app")
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key)
                .compact();
    }

    //2. Token 검증 & claims 반환
    //=> 검증 실패 시 RuntimeException 발생
    public Map<String, Object> validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes("UTF-8"));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException e) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("만료된 토큰입니다. 다시 로그인해주세요.");
        } catch (InvalidClaimException e) {
            throw new RuntimeException("토큰 정보가 올바르지 않습니다.");
        } catch (JwtException e) {
            throw new RuntimeException("토큰 오류가 발생했습니다.");
        } catch (Exception e) {
            throw new RuntimeException("토큰 처리 중 오류가 발생했습니다.");
        }
    }

    //3. memberId 꺼내기 편의 메서드
    public Long getMemberId(String token) {
        Map<String, Object> claims = validateToken(token);
        Object id = claims.get("id");
        if (id instanceof Long) return (Long) id;
        return Long.parseLong(String.valueOf(id));
    }

} //class
