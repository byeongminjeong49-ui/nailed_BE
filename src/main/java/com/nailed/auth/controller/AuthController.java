package com.nailed.auth.controller;

import com.nailed.auth.dto.EmailLoginConfirmRequest;
import com.nailed.auth.dto.EmailLoginRequest;
import com.nailed.auth.dto.EmailVerificationConfirmRequest;
import com.nailed.auth.dto.EmailVerificationRequest;
import com.nailed.auth.dto.LoginRequest;
import com.nailed.auth.dto.PasswordFindRequest;
import com.nailed.auth.dto.RefreshTokenRequest;
import com.nailed.auth.dto.SignUpRequest;
import com.nailed.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//** AuthController
//=> 인증 관련 REST API
//=> demo 프로젝트 UserController 참고
//=> ResponseEntity<?> 직접 반환 (ApiResponse 래퍼 없음)

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final AuthService authService;

    //=> 회원가입: 이메일 인증 요청(mock)
    @PostMapping("/signup/email-verification/request")
    public ResponseEntity<?> requestEmailVerification(@RequestBody EmailVerificationRequest request) {
        try {
            log.info("** 회원가입 이메일 인증 요청(mock) => " + request.getEmail());
            return ResponseEntity.ok(authService.requestEmailVerification(request));
        } catch (Exception e) {
            log.error("** 회원가입 이메일 인증 요청 실패(mock) => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //=> 회원가입: 이메일 인증 확인(mock)
    @PostMapping("/signup/email-verification/confirm")
    public ResponseEntity<?> confirmEmailVerification(@RequestBody EmailVerificationConfirmRequest request) {
        try {
            return ResponseEntity.ok(authService.confirmEmailVerification(request));
        } catch (Exception e) {
            log.error("** 회원가입 이메일 인증 확인 실패(mock) => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //=> 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {
        try {
            log.info("** 회원가입 요청 => " + request.getEmail());
            return ResponseEntity.ok(authService.signUp(request));
        } catch (Exception e) {
            log.error("** 회원가입 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //=> 일반 로그인 (이메일 + 비밀번호)
    @PostMapping({"/login", "/email-login"})
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            log.info("** 로그인 요청 => " + request.getEmail());
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            log.error("** 로그인 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

    //=> 이메일 로그인: 인증코드 요청
    @PostMapping("/email-login/request")
    public ResponseEntity<?> requestEmailLogin(@RequestBody EmailLoginRequest request) {
        try {
            return ResponseEntity.ok(authService.requestEmailLogin(request));
        } catch (Exception e) {
            log.error("** 이메일 로그인 코드 요청 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //=> 이메일 로그인: 인증코드 확인
    @PostMapping("/email-login/confirm")
    public ResponseEntity<?> confirmEmailLogin(@RequestBody EmailLoginConfirmRequest request) {
        try {
            return ResponseEntity.ok(authService.confirmEmailLogin(request));
        } catch (Exception e) {
            log.error("** 이메일 로그인 확인 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

    //=> 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
        } catch (Exception e) {
            log.error("** 토큰 갱신 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    //=> 로그아웃 (클라이언트에서 토큰 삭제 처리)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.info("** 로그아웃 요청");
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    //=> 비밀번호 찾기 (임시 비밀번호 발급)
    @PostMapping("/password/find")
    public ResponseEntity<?> findPassword(@RequestBody PasswordFindRequest request) {
        try {
            log.info("** 비밀번호 찾기 요청 => " + request.getEmail());
            return ResponseEntity.ok(authService.findPassword(request));
        } catch (Exception e) {
            log.error("** 비밀번호 찾기 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

} //class
