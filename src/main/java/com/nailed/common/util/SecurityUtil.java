//package com.nailed.common.util;
//
//import com.nailed.common.exception.CustomException;
//import com.nailed.common.exception.ErrorCode;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
///**
// * 현재 로그인한 회원 정보를 꺼내는 유틸리티
// * JwtAuthenticationFilter가 SecurityContext에 세팅한 principal(memberId)을 반환
// *
// * 사용법 (Service 레이어):
// *   Long memberId = SecurityUtil.getCurrentMemberId();
// *
// * 주의:
// *   인증이 필요 없는 API(상품 목록 등)에서 호출하면 UNAUTHORIZED 예외 발생
// */
//public class SecurityUtil {
//
//    private SecurityUtil() {}
//
//    public static Long getCurrentMemberId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null
//                || !authentication.isAuthenticated()
//                || authentication.getPrincipal() == null) {
//            throw new CustomException(ErrorCode.UNAUTHORIZED);
//        }
//
//        Object principal = authentication.getPrincipal();
//
//        if (principal instanceof Long memberId) {
//            return memberId;
//        }
//
//        try {
//            return Long.parseLong(principal.toString());
//        } catch (NumberFormatException e) {
//            throw new CustomException(ErrorCode.UNAUTHORIZED);
//        }
//    }
//}
