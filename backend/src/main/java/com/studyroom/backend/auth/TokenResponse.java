package com.studyroom.backend.auth;

/**
 * 로그인·재발급 응답 본문. refresh token은 본문이 아니라 HttpOnly 쿠키로만 보낸다.
 */
public record TokenResponse(String accessToken) {
}
