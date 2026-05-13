package com.nailed.web.member.service;

import com.nailed.web.member.dto.MemberProfileUpdateRequest;
import com.nailed.web.member.dto.MyPageResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//** MemberService
//=> 회원 관련 비즈니스 로직 (마이페이지)
//=> memberId 는 Controller 에서 @AuthenticationPrincipal 로 받아서 전달
//=> demo 프로젝트 MemberServiceImpl 참고

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {

    private final MemberRepository memberRepository;

    //=> 마이페이지 조회
    public MyPageResponse getMyPage(Long memberId) {
        Member member = getMember(memberId);
        return MyPageResponse.from(member);
    }

    //=> 프로필 수정 (닉네임)
    @Transactional
    public MyPageResponse updateProfile(Long memberId, MemberProfileUpdateRequest request) {
        Member member = getMember(memberId);
        member.changeNickname(request.getNickname());
        log.info("** 프로필 수정 성공 => memberId=" + memberId);
        return MyPageResponse.from(member);
    }

    //=> 회원 탈퇴
    @Transactional
    public void withdraw(Long memberId) {
        Member member = getMember(memberId);
        member.withdraw();
        log.info("** 회원 탈퇴 성공 => memberId=" + memberId);
    }

    //--- 내부 메서드 -------------------------------------------

    //=> memberId 로 활성 회원 조회
    //=> 탈퇴/정지/밴 상태이면 RuntimeException 발생
    private Member getMember(Long memberId) {
        Optional<Member> result = memberRepository.findByIdAndDeletedAtIsNull(memberId);
        if (result.isEmpty()) {
            throw new RuntimeException("회원을 찾을 수 없습니다.");
        }
        Member member = result.get();

        switch (member.getMemberStatus()) {
            case ACTIVE   -> { return member; }
            case WITHDRAWN -> throw new RuntimeException("탈퇴한 회원입니다.");
            case SUSPENDED -> throw new RuntimeException("일시 정지된 계정입니다.");
            case BANNED    -> throw new RuntimeException("영구 차단된 계정입니다.");
            default        -> throw new RuntimeException("접근할 수 없는 계정입니다.");
        }
    }

} //class
