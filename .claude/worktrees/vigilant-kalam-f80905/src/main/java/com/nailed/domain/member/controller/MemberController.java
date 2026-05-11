package com.nailed.domain.member.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.member.dto.MemberRequest;
import com.nailed.domain.member.dto.MemberResponse;
import com.nailed.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse.Detail>> getMyProfile(@PathVariable String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyProfile(memberId)));
    }

    @PutMapping("/{memberId}/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @PathVariable String memberId,
            @Valid @RequestBody MemberRequest.UpdateProfile request) {
        memberService.updateProfile(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("프로필이 수정되었습니다.", null));
    }

    @PutMapping("/{memberId}/bank-info")
    public ResponseEntity<ApiResponse<Void>> updateBankInfo(
            @PathVariable String memberId,
            @Valid @RequestBody MemberRequest.UpdateBankInfo request) {
        memberService.updateBankInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("정산 계좌가 등록되었습니다.", null));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> withdraw(@PathVariable String memberId) {
        memberService.withdraw(memberId);
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null));
    }
}
