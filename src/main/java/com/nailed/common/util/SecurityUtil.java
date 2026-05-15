package com.nailed.common.util;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 현재 로그인한 회원 정보를 꺼내는 유틸리티
 * JwtAuthenticationFilter가 SecurityContext에 세팅한 principal(memberId)을 반환
 *
 * 사용법 (Service 레이어):
 *   String memberId = SecurityUtil.getCurrentMemberId();
 *
 * 주의:
 *   - members.member_id 는 VARCHAR(20) (예: MEMBER_001 형태)
 *   - 인증이 필요 없는 API(상품 목록 등)에서 호출하면 UNAUTHORIZED 예외 발생
 */
public class SecurityUtil {

    private SecurityUtil() {}

    public static String getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String memberId) {
            return memberId;
        }

        // 다른 타입으로 들어온 경우 toString 으로 변환
        String memberId = principal.toString();
        if (memberId.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return memberId;
    }
}
