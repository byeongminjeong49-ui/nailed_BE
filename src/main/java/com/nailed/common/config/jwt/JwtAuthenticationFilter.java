package com.nailed.common.config.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//** JwtAuthenticationFilter
//=> 모든 요청에서 JWT를 검사하는 Security 필터
//=> Authorization: Bearer {token} 헤더를 파싱해 SecurityContext에 인증 정보 세팅
//=> demo 프로젝트 JwtAuthenticationFilter 참고

@Component
@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            //1) request 에서 토큰 가져오기
            String token = parseBearerToken(request);
            log.info("** JwtAuthenticationFilter, token 확인 => " + token);

            if (token != null && !token.equalsIgnoreCase("null")) {

                //2) 토큰 검증 & claims 가져오기
                Map<String, Object> claims = tokenProvider.validateToken(token);
                log.info("** JWT claims: " + claims);

                Long memberId = tokenProvider.getMemberId(token);

                //=> roleList 타입 안전하게 파싱
                ObjectMapper mapper = new ObjectMapper();
                List<String> roleList =
                        mapper.convertValue(claims.get("roleList"),
                                new TypeReference<List<String>>() {});

                //3) 인증 완료 & SecurityContext 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberId,   // principal → @AuthenticationPrincipal Long memberId
                                null,
                                roleList.stream()
                                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                        .collect(Collectors.toList())
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } //if

        } catch (Exception e) {
            log.error("** JwtAuthenticationFilter 오류 => " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    } //doFilterInternal

    //=> Authorization 헤더에서 Bearer 토큰 추출
    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

} //class
