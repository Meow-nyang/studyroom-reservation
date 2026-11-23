package com.studyroom.backend.auth.refresh;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.studyroom.backend.auth.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;

/**
 * refresh token 쿠키를 만들고 지운다.
 * - HttpOnly: JavaScript가 읽을 수 없다 (XSS가 나도 토큰을 못 가져감)
 * - SameSite=Strict: 다른 사이트에서 시작된 요청에는 붙지 않는다 (CSRF 방어)
 * - Path=/api/auth: 재발급·로그아웃 요청에만 붙는다. 다른 API 요청에는 아예 실려 가지 않는다
 * - Secure: HTTPS에서만 보낸다. 로컬(HTTP)에서는 꺼 둔다
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookie {

    public static final String NAME = "refresh_token";
    private static final String PATH = "/api/auth";

    private final JwtProperties jwtProperties;

    public String create(String rawToken) {
        return base(rawToken)
                .maxAge(jwtProperties.refreshTokenValidity())
                .build()
                .toString();
    }

    // 같은 이름·경로에 수명 0으로 덮어써서 브라우저가 지우게 한다
    public String clear() {
        return base("")
                .maxAge(0)
                .build()
                .toString();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Strict")
                .path(PATH);
    }
}
