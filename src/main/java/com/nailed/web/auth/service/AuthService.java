package com.nailed.web.auth.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.auth.dto.AuthRequest;
import com.nailed.web.auth.dto.AuthResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String MOCK_PASSWORD_PREFIX = "{mock}";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
   

    public AuthResponse.DuplicateCheck checkEmail(String email) {
        return new AuthResponse.DuplicateCheck(memberRepository.existsByEmail(email));
    }

    public AuthResponse.DuplicateCheck checkNickname(String nickname) {
        return new AuthResponse.DuplicateCheck(memberRepository.existsByNickname(nickname));
    }

    @Transactional
    public AuthResponse.Signup signup(AuthRequest.Signup request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        

        Member member = Member.builder()
                .memberId(generateMemberId())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .name(request.name())
                .marketingAgreed(request.marketingAgreed())
                .build();

        return AuthResponse.Signup.from(memberRepository.save(member));
    }

    public AuthResponse.Login login(AuthRequest.Login request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));

        validateMemberStatus(member);

        if (!matchesMockPassword(request.password(), member.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        return AuthResponse.Login.from(member);
    }

  

    public AuthResponse.SimpleResult requestPasswordReset(AuthRequest.PasswordResetRequest request) {
        if (memberRepository.findByEmail(request.email()).isEmpty()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return new AuthResponse.SimpleResult(true);
    }

    private String generateMemberId() {
        String memberId;
        do {
            memberId = "M" + System.currentTimeMillis();
        } while (memberRepository.existsById(memberId));
        return memberId;
    }

    private boolean matchesMockPassword(String rawPassword, String storedPassword) {
        return passwordEncoder.matches(rawPassword, storedPassword)
                || (MOCK_PASSWORD_PREFIX + rawPassword).equals(storedPassword)
                || rawPassword.equals(storedPassword);
    }

    private void validateMemberStatus(Member member) {
        String status = member.getMemberStatus();

        if ("WITHDRAWN".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_WITHDRAWN);
        }
        if ("SUSPEND".equals(status) || "SUSPENDED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_SUSPENDED);
        }
        if ("BANNED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_BANNED);
        }
        if ("LOCKED".equals(status)) {
            throw new CustomException(ErrorCode.MEMBER_LOCKED);
        }
    }
}
