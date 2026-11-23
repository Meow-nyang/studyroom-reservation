package com.studyroom.backend.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.auth.jwt.JwtProvider;
import com.studyroom.backend.auth.refresh.RefreshTokenService;
import com.studyroom.backend.common.exception.ConflictException;
import com.studyroom.backend.common.exception.UnauthorizedException;
import com.studyroom.backend.member.Member;
import com.studyroom.backend.member.MemberRepository;
import com.studyroom.backend.member.MemberResponse;
import com.studyroom.backend.member.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final String DUPLICATE_EMAIL_MESSAGE = "이미 가입된 이메일이에요.";
    private static final String LOGIN_FAILED_MESSAGE = "이메일 또는 비밀번호가 올바르지 않아요.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public MemberResponse signup(SignupRequest request) {
        String email = Member.normalizeEmail(request.email());
        if (memberRepository.existsByEmail(email)) {
            throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
        }

        Member member = Member.builder()
                .email(email)
                .encodedPassword(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .build();

        try {
            // 위의 existsByEmail 검사와 저장 사이에 같은 이메일로 가입한 요청이 끼어들 수 있다.
            // 그때는 DB의 유니크 제약이 막아 주므로, 그 에러를 같은 409로 바꿔서 알려 준다.
            return MemberResponse.from(memberRepository.saveAndFlush(member));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
        }
    }

    public IssuedTokens login(LoginRequest request) {
        // 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 문구로 응답한다.
        // 다르게 응답하면 "이 이메일은 가입돼 있다"는 정보가 새어 나간다.
        Member member = memberRepository.findByEmail(Member.normalizeEmail(request.email()))
                .filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
                .orElseThrow(() -> new UnauthorizedException(LOGIN_FAILED_MESSAGE));

        return new IssuedTokens(
                jwtProvider.createAccessToken(member.getId(), member.getRole()),
                refreshTokenService.issue(member));
    }

    public IssuedTokens refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("다시 로그인해 주세요.");
        }
        RefreshTokenService.Rotated rotated = refreshTokenService.rotate(rawRefreshToken);
        Member member = rotated.member();
        return new IssuedTokens(
                jwtProvider.createAccessToken(member.getId(), member.getRole()),
                rotated.refreshToken());
    }

    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        }
    }

    // 컨트롤러로 넘기는 두 토큰. access token은 응답 본문으로, refresh token은 쿠키로 나간다.
    public record IssuedTokens(String accessToken, String refreshToken) {
    }
}
