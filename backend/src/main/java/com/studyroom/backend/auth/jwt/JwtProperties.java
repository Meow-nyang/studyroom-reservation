package com.studyroom.backend.auth.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 jwt.* 설정을 담는다. 예: jwt.access-token-validity: 30m → accessTokenValidity
 */
@ConfigurationProperties("jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenValidity,
        Duration refreshTokenValidity,
        boolean cookieSecure) {
}
