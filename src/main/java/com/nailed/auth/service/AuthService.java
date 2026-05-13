package com.nailed.auth.service;

import com.nailed.auth.entity.EmailLoginVerification;
import com.nailed.auth.repository.EmailLoginVerificationRepository;
import com.nailed.auth.dto.EmailLoginConfirmRequest;
import com.nailed.auth.dto.EmailLoginRequest;
import com.nailed.auth.dto.EmailVerificationConfirmRequest;
import com.nailed.auth.dto.EmailVerificationRequest;
import com.nailed.auth.dto.LoginRequest;
import com.nailed.auth.dto.PasswordFindRequest;
import com.nailed.auth.dto.SignUpRequest;
import com.nailed.common.config.jwt.TokenProvider;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

//** AuthService
//=> 인증 관련 비즈니스 로직
//=> 회원가입, 로그인, 비밀번호 찾기
//=> demo 프로젝트 MemberService 참고

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final MemberRepository memberRepository;
    private final EmailLoginVerificationRepository emailLoginVerificationRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    //1. 회원가입 이메일 인증 요청(mock)
    public Map<String, Object> requestEmailVerification(EmailVerificationRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("** 회원가입 이메일 인증 요청(mock) => " + email);

        Map<String, Object> result = new HashMap<>();
        result.put("alertMessage", "이메일 인증번호가 발급되었습니다.");
        return result;
    }

    //2. 회원가입 이메일 인증 확인(mock)
    public Map<String, Object> confirmEmailVerification(EmailVerificationConfirmRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("** 회원가입 이메일 인증 완료(mock) => " + email);

        Map<String, Object> result = new HashMap<>();
        result.put("alertMessage", "이메일 인증이 완료되었습니다.");
        return result;
    }

    //3. 회원가입
    @Transactional
    public Map<String, Object> signUp(SignUpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        //=> 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        //=> 전화번호 중복 확인
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("이미 가입된 전화번호입니다.");
        }
        Member member = Member.createUser(
                nextMemberId(),
                email,
                passwordEncoder.encode(request.getPassword()),
                phoneNumber
        );
        memberRepository.save(member);
        log.info("** 회원가입 성공 => " + email);

        Map<String, Object> result = new HashMap<>();
        result.put("alertMessage", "회원 가입이 완료되었습니다.");
        result.put("value", email);
        return result;
    }

    //4. 일반 로그인 (이메일 + 비밀번호)
    public Map<String, Object> login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        //=> 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        //=> 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("** 로그인 성공 => " + email);
        return createTokenResponse(member);
    }

    //5. 이메일 로그인 코드 요청
    @Transactional
    public Map<String, Object> requestEmailLogin(EmailLoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입된 회원을 찾을 수 없습니다."));

        String code = createCode(6);
        emailLoginVerificationRepository.save(EmailLoginVerification.issue(email, code));
        log.info("** 이메일 로그인 코드 발급 => " + email + ", code=" + code);

        //=> 실제 서비스: 이메일 발송 (지금은 코드 직접 반환)
        Map<String, Object> result = new HashMap<>();
        result.put("alertMessage", "이메일 로그인 인증번호가 발급되었습니다.");
        result.put("value", code); // 개발환경에서 코드 직접 확인
        return result;
    }

    //6. 이메일 로그인 코드 확인
    @Transactional
    public Map<String, Object> confirmEmailLogin(EmailLoginConfirmRequest request) {
        String email = normalizeEmail(request.getEmail());

        EmailLoginVerification verification = emailLoginVerificationRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new RuntimeException("이메일 로그인 인증이 필요합니다."));

        if (!verification.canVerify(request.getVerificationCode())) {
            throw new RuntimeException("인증번호가 올바르지 않거나 만료되었습니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        verification.complete();
        log.info("** 이메일 로그인 성공 => " + email);
        return createTokenResponse(member);
    }

    //7. Token 갱신
    public Map<String, Object> refresh(String refreshToken) {
        Map<String, Object> claims = tokenProvider.validateToken(refreshToken);
        Long memberId = tokenProvider.getMemberId(refreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        return createTokenResponse(member);
    }

    //8. 비밀번호 찾기 (임시 비밀번호 발급)
    @Transactional
    public Map<String, Object> findPassword(PasswordFindRequest request) {
        Member member = memberRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new RuntimeException("가입된 회원을 찾을 수 없습니다."));

        String tempPassword = createCode(8);
        member.changePassword(passwordEncoder.encode(tempPassword));
        log.info("** 임시 비밀번호 발급 => " + request.getEmail());

        Map<String, Object> result = new HashMap<>();
        result.put("alertMessage", "임시 비밀번호가 발급되었습니다.");
        result.put("value", tempPassword);
        return result;
    }

    //--- 내부 메서드 ----------------------------------------

    //=> 다음 memberId 생성 (현재 최대 ID + 1)
    private Long nextMemberId() {
        return memberRepository.findMaxId() + 1;
    }

    //=> 로그인 성공 시 토큰 응답 생성
    private Map<String, Object> createTokenResponse(Member member) {
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member.getId());
        result.put("email", member.getEmail());
        result.put("accessToken",  tokenProvider.createToken(member.claimList(), 24 * 60)); // 1일
        result.put("refreshToken", tokenProvider.createToken(member.refreshClaimList(), 7 * 24 * 60)); // 7일
        return result;
    }

    //=> 랜덤 숫자 코드 생성
    private String createCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }

} //class
