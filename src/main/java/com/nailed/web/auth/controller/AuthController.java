package com.nailed.web.auth.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.auth.dto.AuthRequest;
import com.nailed.web.auth.dto.AuthResponse;
import com.nailed.web.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/api/auth/check-userid")
    public ResponseEntity<ApiResponse<AuthResponse.DuplicateCheck>> checkUserid(
            @RequestParam String userid) {
        return ResponseEntity.ok(ApiResponse.success(authService.checkUserid(userid)));
    }

    @GetMapping("/api/auth/check-nickname")
    public ResponseEntity<ApiResponse<AuthResponse.DuplicateCheck>> checkNickname(
            @RequestParam String nickname) {
        return ResponseEntity.ok(ApiResponse.success(authService.checkNickname(nickname)));
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<ApiResponse<AuthResponse.Signup>> signup(
            @Valid @RequestBody AuthRequest.Signup request) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request)));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse.Login>> login(
            @Valid @RequestBody AuthRequest.Login request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

  

    @PostMapping("/api/auth/password/reset-request")
    public ResponseEntity<ApiResponse<AuthResponse.PasswordReset>> requestPasswordReset(
            @Valid @RequestBody AuthRequest.PasswordResetRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.requestPasswordReset(request)));
    }
}
