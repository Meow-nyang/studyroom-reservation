package com.studyroom.backend.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.studyroom.backend.member.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * access token(JWT)을 만들고 검증한다.
 * 토큰 안에는 회원 id(sub)와 권한(role)만 넣는다. 이메일이나 이름 같은 개인 정보는 넣지 않는다.
 * (JWT 본문은 암호화가 아니라 Base64 인코딩이라 누구나 읽을 수 있다)
 */
@Component
public class JwtProvider {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtProvider(JwtProperties properties) {
        byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("jwt.secret은 32바이트 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.properties = properties;
    }

    public String createAccessToken(Long memberId, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenValidity())))
                .signWith(key)
                .compact();
    }

    /**
     * 서명과 만료 시각을 확인하고 회원 정보를 꺼낸다.
     * 위조됐거나 만료됐거나 형식이 틀리면 JwtException을 던진다.
     */
    public AuthMember parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        try {
            return new AuthMember(
                    Long.valueOf(claims.getSubject()),
                    Role.valueOf(claims.get(ROLE_CLAIM, String.class)));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new JwtException("토큰 내용이 올바르지 않습니다.", e);
        }
    }
}
