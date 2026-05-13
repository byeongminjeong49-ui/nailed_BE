package com.nailed.web.member.controller;

import com.nailed.web.member.dto.MemberProfileUpdateRequest;
import com.nailed.web.member.dto.MyPageResponse;
import com.nailed.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//** MemberController
//=> 회원 마이페이지 REST API
//=> @AuthenticationPrincipal Long memberId
//    → JwtAuthenticationFilter 가 SecurityContext 에 등록한 principal(memberId) 을 주입받음
//    → demo 프로젝트 UserController 의 @AuthenticationPrincipal String userId 참고
//=> ResponseEntity<?> 직접 반환 (ApiResponse 래퍼 없음)

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final MemberService memberService;

    //=> 마이페이지 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyPage(@AuthenticationPrincipal Long memberId) {
        try {
            log.info("** 마이페이지 조회 => memberId=" + memberId);
            MyPageResponse response = memberService.getMyPage(memberId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("** 마이페이지 조회 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

    //=> 프로필 수정 (닉네임)
    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Long memberId,
                                           @RequestBody MemberProfileUpdateRequest request) {
        try {
            log.info("** 프로필 수정 요청 => memberId=" + memberId);
            MyPageResponse response = memberService.updateProfile(memberId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("** 프로필 수정 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

    //=> 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal Long memberId) {
        try {
            log.info("** 회원 탈퇴 요청 => memberId=" + memberId);
            memberService.withdraw(memberId);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            log.error("** 회원 탈퇴 실패 => " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

} //class
