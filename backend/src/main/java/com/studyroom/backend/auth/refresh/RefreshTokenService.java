package com.studyroom.backend.auth.refresh;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyroom.backend.auth.jwt.JwtProperties;
import com.studyroom.backend.common.exception.UnauthorizedException;
import com.studyroom.backend.member.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private static final String INVALID_MESSAGE = "다시 로그인해 주세요.";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * 새 refresh token을 만들어 해시를 저장하고, 원문을 돌려준다. 원문은 쿠키로만 나간다.
     */
    public String issue(Member member) {
        String rawToken = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plus(jwtProperties.refreshTokenValidity());
        refreshTokenRepository.save(new RefreshToken(member, hash(rawToken), expiresAt));
        return rawToken;
    }

    /**
     * rotation: 받은 토큰을 지우고 새 토큰을 발급한다. 한 번 쓴 토큰은 다시 쓸 수 없다.
     * @return 토큰 주인과 새 refresh token 원문
     */
    public Rotated rotate(String rawToken) {
        RefreshToken saved = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException(INVALID_MESSAGE));
        Member member = saved.getMember();

        if (saved.isExpired(LocalDateTime.now())) {
            refreshTokenRepository.deleteByIdAndCount(saved.getId());
            throw new UnauthorizedException(INVALID_MESSAGE);
        }

        // 조회와 삭제 사이에 같은 토큰으로 온 다른 요청이 먼저 지웠다면 0줄이 지워진다 → 이 요청은 실패
        if (refreshTokenRepository.deleteByIdAndCount(saved.getId()) == 0) {
            throw new UnauthorizedException(INVALID_MESSAGE);
        }

        return new Rotated(member, issue(member));
    }

    public void revoke(String rawToken) {
        refreshTokenRepository.deleteByTokenHash(hash(rawToken));
    }

    public record Rotated(Member member, String refreshToken) {
    }

    // 32바이트(256비트) 무작위 값 → URL과 쿠키에 안전한 문자열
    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
