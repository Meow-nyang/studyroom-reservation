package com.studyroom.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 인증에 실패했을 때. 예: 이메일이나 비밀번호가 틀림, refresh token이 없거나 만료됨
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
